package org.opentripplanner.assertions;

import java.util.ArrayList;
import java.util.List;

/** Converts structured match results to human-readable diagnostic text. */
public final class MatchResultFormatter {
  private final List<String> lines = new ArrayList<>();

  private MatchResultFormatter() {}

  /** Returns an indented report with line breaks between entries and no trailing line break. */
  public static String format(PlanMatchResult result) {
    var formatter = new MatchResultFormatter();
    formatter.appendPlan(result, 0);
    return String.join("\n", formatter.lines);
  }

  private void appendPlan(PlanMatchResult result, int depth) {
    String heading = result.description() + (result.matches() ? " [matched]" : " [failed]");
    if (result.matchingMode() != null) heading += " (" + result.matchingMode() + ")";
    line(depth, heading);
    appendExpectations(result, depth + 1);
    result
        .itineraryResults()
        .forEach(itinerary -> appendItinerary(itinerary, result.matchingMode(), depth + 1));
    result.children().forEach(child -> appendPlan(child, depth + 1));
  }

  private void appendExpectations(PlanMatchResult result, int depth) {
    for (int i = 0; i < result.expectedLegs().size(); i++) {
      line(depth, "Leg " + (i + 1) + ": " + String.join(", ", result.expectedLegs().get(i)));
    }
    result.itineraryCriteria().forEach(criterion -> line(depth, criterion));
  }

  private void appendItinerary(ItineraryMatchResult result, TransitLegMatching mode, int depth) {
    line(
        depth,
        "Itinerary "
            + (result.itineraryIndex() + 1)
            + (result.isSuccess() ? " matched" : " failed"));
    result.errors().forEach(error -> line(depth + 1, error));
    if (!result.extraMatches().isEmpty()) {
      line(
          depth + 1, "Itinerary contains additional transit legs: " + result.extraMatches().size());
    }
    result.legResults().forEach(requirement -> appendRequirement(requirement, mode, depth + 1));
    var legs = result.itinerary().legs();
    for (int i = 0; i < legs.size(); i++) {
      line(depth + 1, "Actual leg " + (i + 1) + ": " + legs.get(i).formatLegDescription());
    }
  }

  private void appendRequirement(LegRequirementResult result, TransitLegMatching mode, int depth) {
    line(
        depth,
        "Requirement "
            + (result.expectedLegIndex() + 1)
            + ": "
            + requirementDescription(result, mode));
    appendCriteria("Criteria", result.expectedCriteria(), depth + 1);
    result.candidates().forEach(candidate -> appendCandidate(candidate, depth + 1));
  }

  private static String requirementDescription(
      LegRequirementResult result, TransitLegMatching mode) {
    int position = result.expectedLegIndex() + 1;
    return switch (result.status()) {
      case MATCHED ->
          "matched transit leg " + (result.matchedLeg().orElseThrow().actualLegIndex() + 1);
      case MISSING_LEG ->
          mode == TransitLegMatching.EXACT_ORDERED
              ? "No transit leg at position " + position
              : "No transit legs are available";
      case CRITERIA_MISMATCH ->
          mode == TransitLegMatching.EXACT_ORDERED
              ? "Transit leg at position " + position + " does not match criteria"
              : "No transit leg matches all criteria";
      case ALREADY_ASSIGNED -> "matching transit legs are already assigned";
      case OUT_OF_ORDER -> "matching transit legs are not in travel order";
    };
  }

  private void appendCandidate(LegMatchResult result, int depth) {
    line(
        depth,
        "Candidate transit leg "
            + (result.actualLegIndex() + 1)
            + ": "
            + result.leg().formatLegDescription());
    appendCriteria("Matched", result.matchingCriteria(), depth + 1);
    appendCriteria("Missing", result.missingCriteria(), depth + 1);
  }

  private void appendCriteria(String label, List<String> criteria, int depth) {
    if (!criteria.isEmpty()) line(depth, label + ": " + String.join(", ", criteria));
  }

  private void line(int depth, String text) {
    lines.add("  ".repeat(depth) + text);
  }
}
