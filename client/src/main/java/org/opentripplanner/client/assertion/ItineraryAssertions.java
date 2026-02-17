package org.opentripplanner.client.assertion;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.opentripplanner.api.types.Route;
import org.opentripplanner.client.model.Itinerary;
import org.opentripplanner.client.model.Leg;
import org.opentripplanner.client.model.TripPlan;

/**
 * A fluent API for testing OTP itineraries against specific criteria.
 *
 * <p>This class allows you to define required legs and validate that at least one itinerary in a
 * trip plan matches all required criteria.
 */
public class ItineraryAssertions {

  // Each entry contains the complete set of criteria that one leg must satisfy.
  private final List<List<LegCriterion>> distinctLegCriteria = new ArrayList<>();
  private List<LegCriterion> currentLegCriteria;
  private boolean strictTransitMatching = false;

  /**
   * Adds a new distinct leg criterion set and moves the current leg pointer to it.
   *
   * <p>After calling this, fluent criterion methods apply to the new expected leg.
   */
  public ItineraryAssertions hasLeg() {
    currentLegCriteria = new ArrayList<>();
    distinctLegCriteria.add(currentLegCriteria);
    return this;
  }

  public ItineraryAssertions withRouteLongName(String... longNames) {
    var message = "route '%s'".formatted(Arrays.toString(longNames));
    currentLegCriteria.add(
        new LegCriterion(
            message,
            state -> {
              Leg leg = state.getLeg();
              String longName = routeLongName(leg.route());
              boolean matches =
                  leg.isTransit()
                      && longName != null
                      && Arrays.asList(longNames).contains(longName);
              if (matches) {
                state.addMatch(message);
              } else {
                state.addFailure(message);
              }
            }));
    return this;
  }

  public ItineraryAssertions withMaxDuration(Duration duration) {
    var message = "duration '%s'".formatted(duration);
    currentLegCriteria.add(
        new LegCriterion(
            message,
            state -> {
              Leg leg = state.getLeg();
              boolean matches = leg.isTransit() && leg.duration().compareTo(duration) < 1;
              if (matches) {
                state.addMatch(message);
              } else {
                state.addFailure(message);
              }
            }));
    return this;
  }

  public ItineraryAssertions withRouteShortName(String... shortNames) {
    var message = "route '%s'".formatted(Arrays.toString(shortNames));
    currentLegCriteria.add(
        new LegCriterion(
            message,
            state -> {
              Leg leg = state.getLeg();
              String shortName = routeShortName(leg.route());
              boolean matches =
                  leg.isTransit()
                      && shortName != null
                      && Arrays.asList(shortNames).contains(shortName);
              if (matches) {
                state.addMatch(message);
              } else {
                state.addFailure(message);
              }
            }));
    return this;
  }

  public ItineraryAssertions withFarePrice(float price, String riderCategoryId, String mediumId) {
    var message = "fare $%.2f".formatted(price);
    currentLegCriteria.add(
        new LegCriterion(
            message,
            state -> {
              Leg leg = state.getLeg();
              boolean matches =
                  leg.fareProducts().stream()
                      .filter(fp -> fp.product().riderCategory().isPresent())
                      .filter(fp -> fp.product().medium().isPresent())
                      .filter(fp -> fp.product().riderCategory().get().id().equals(riderCategoryId))
                      .filter(fp -> fp.product().medium().get().id().equals(mediumId))
                      .anyMatch(fp -> fp.product().price().amount().floatValue() == price);
              if (matches) {
                state.addMatch(message);
              } else {
                state.addFailure(message);
              }
            }));
    return this;
  }

  public ItineraryAssertions interlinedWithPreviousLeg() {
    currentLegCriteria.add(
        new LegCriterion(
            "interlined with previous leg",
            state -> {
              Leg leg = state.getLeg();
              if (leg.interlineWithPreviousLeg()) {
                state.addMatch("interlined with previous leg");
              } else {
                state.addFailure("interlined with previous leg");
              }
            }));
    return this;
  }

  public ItineraryAssertions withMode(String mode) {
    var message = "mode %s".formatted(mode);
    currentLegCriteria.add(
        new LegCriterion(
            message,
            state -> {
              Leg leg = state.getLeg();
              boolean matches = leg.mode().toString().equals(mode);
              if (matches) {
                state.addMatch(message);
              } else {
                state.addFailure(message);
              }
            }));
    return this;
  }

  /**
   * Enables strict transit matching, requiring no unmatched transit legs in the chosen itinerary.
   */
  public ItineraryAssertions withStrictTransitMatching() {
    this.strictTransitMatching = true;
    return this;
  }

  /** Asserts that at least one itinerary in the given trip plan matches all configured criteria. */
  public void assertMatches(TripPlan tripPlan) {
    List<ItineraryMatchResult> failedResults = new ArrayList<>();

    for (Itinerary itinerary : tripPlan.itineraries()) {
      ItineraryMatchResult result = matchesAllLegs(itinerary);
      if (result.isSuccess()) {
        return;
      }
      failedResults.add(result);
    }

    String strictMatchingText = strictTransitMatching ? " with strict transit matching" : "";
    String header =
        "No itinerary found matching all required legs%s:%n".formatted(strictMatchingText);

    StringBuilder criteriaSection = new StringBuilder();
    for (int i = 0; i < distinctLegCriteria.size(); i++) {
      criteriaSection.append(
          "Leg %d criteria:%n%s%n".formatted(i + 1, describeCriteria(distinctLegCriteria.get(i))));
    }

    StringBuilder failuresSection = new StringBuilder();
    failuresSection.append("%nFailures by itinerary:%n");

    for (int i = 0; i < failedResults.size(); i++) {
      ItineraryMatchResult result = failedResults.get(i);
      Itinerary itinerary = tripPlan.itineraries().get(i);
      failuresSection.append("Itinerary %d:%n".formatted(i + 1));

      result.errors().forEach(err -> failuresSection.append("  - %s%n".formatted(err)));

      if (!result.getPartialMatches().isEmpty()) {
        failuresSection.append("  Partial matches:%n");
        result
            .getPartialMatches()
            .forEach(
                match ->
                    failuresSection.append(
                        "    - Leg with %s but missing %s%n"
                            .formatted(match.getMatchingCriteria(), match.getMissingCriteria())));
      }

      failuresSection.append("  Actual itinerary:%n");
      for (int legIndex = 0; legIndex < itinerary.legs().size(); legIndex++) {
        Leg leg = itinerary.legs().get(legIndex);
        failuresSection.append("    Leg %d: %s".formatted(legIndex + 1, formatLegDescription(leg)));
      }
      failuresSection.append("%n");
    }

    String fullError = header + criteriaSection + failuresSection;
    throw new ItineraryAssertionError(fullError, failedResults);
  }

  /**
   * Checks each requirement to ensure some leg on the itinerary matches.
   *
   * <p>If strict transit matching is enabled, all transit legs must match some requirement.
   */
  private ItineraryMatchResult matchesAllLegs(Itinerary itinerary) {
    List<Leg> remainingLegs = new ArrayList<>(itinerary.legs());
    List<String> errors = new ArrayList<>();
    List<LegMatchingState> completeMatches = new ArrayList<>();
    List<LegMatchingState> partialMatches = new ArrayList<>();

    if (distinctLegCriteria.isEmpty()) {
      throw new IllegalArgumentException("No leg criteria specified");
    }

    for (var criteriaIndex = 0; criteriaIndex < distinctLegCriteria.size(); criteriaIndex++) {
      List<LegCriterion> criteriaSet = distinctLegCriteria.get(criteriaIndex);
      boolean foundMatch = false;

      if (criteriaSet.isEmpty()) {
        throw new IllegalArgumentException(
            "No leg criteria specified for criteria set " + (criteriaIndex + 1));
      }

      for (int i = 0; i < remainingLegs.size(); i++) {
        Leg leg = remainingLegs.get(i);
        LegMatchingState state = new LegMatchingState(leg);
        criteriaSet.forEach(criterion -> criterion.test().accept(state));

        if (state.isFullMatch()) {
          remainingLegs.remove(i);
          foundMatch = true;
          completeMatches.add(state);
          break;
        } else if (state.hasAnyMatch()) {
          partialMatches.add(state);
        }
      }

      if (!foundMatch) {
        errors.add(
            "No leg found matching criteria set %d: %s"
                .formatted(criteriaIndex + 1, describeCriteria(criteriaSet).trim()));
      }
    }

    List<Leg> extraLegs = new ArrayList<>();
    if (strictTransitMatching && errors.isEmpty()) {
      List<Leg> additionalTransitLegs = remainingLegs.stream().filter(Leg::isTransit).toList();

      if (!additionalTransitLegs.isEmpty()) {
        extraLegs.addAll(additionalTransitLegs);
        String extraLegNames =
            additionalTransitLegs.stream()
                .map(leg -> routeDisplayName(leg.route(), leg.mode().toString()))
                .collect(Collectors.joining(" "));

        errors.add(
            "Itinerary contains additional transit legs when strict matching is enabled: %s"
                .formatted(extraLegNames));
      }
    }

    if (errors.isEmpty()) {
      return ItineraryMatchResult.success(completeMatches);
    }

    return new ItineraryMatchResult(completeMatches, partialMatches, extraLegs, errors);
  }

  private String formatLegDescription(Leg leg) {
    if (leg.isTransit()) {
      String routeName = routeDisplayName(leg.route(), leg.mode().toString());
      String interlinedText = leg.interlineWithPreviousLeg() ? " (interlined)" : "";
      return "TRANSIT - Route: %s%s, From: %s, To: %s%n"
          .formatted(routeName, interlinedText, leg.from().name(), leg.to().name());
    }

    return "%s - From: %s, To: %s, Distance: %.0fm%n"
        .formatted(
            leg.mode().toString().toUpperCase(),
            leg.from().name(),
            leg.to().name(),
            leg.distance());
  }

  private String describeCriteria(List<LegCriterion> criteriaSet) {
    StringBuilder message = new StringBuilder();
    for (LegCriterion criterion : criteriaSet) {
      message.append(criterion.message()).append("\n");
    }
    return message.toString();
  }

  private static String routeDisplayName(Route route, String fallback) {
    String shortName = routeShortName(route);
    if (shortName != null) {
      return shortName;
    }
    String longName = routeLongName(route);
    if (longName != null) {
      return longName;
    }
    return fallback;
  }

  private static String routeShortName(Route route) {
    return route == null ? null : route.getShortName();
  }

  private static String routeLongName(Route route) {
    return route == null ? null : route.getLongName();
  }
}
