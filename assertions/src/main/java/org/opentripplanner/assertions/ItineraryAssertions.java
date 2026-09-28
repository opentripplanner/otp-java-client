package org.opentripplanner.assertions;

import static org.opentripplanner.assertions.LegCriterion.describeCriteria;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.opentripplanner.client.model.Itinerary;
import org.opentripplanner.client.model.Leg;
import org.opentripplanner.client.model.Place;
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

  private void addCurrentLegCriterion(String message, Predicate<Leg> predicate) {
    currentLegCriteria.add(new LegCriterion(message, predicate));
  }

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
    addCurrentLegCriterion(
        "route '%s'".formatted(Arrays.toString(longNames)),
        leg -> {
          String longName = leg.route().getLongName();
          return leg.isTransit() && longName != null && Arrays.asList(longNames).contains(longName);
        });
    return this;
  }

  public ItineraryAssertions withMaxDuration(Duration duration) {
    addCurrentLegCriterion(
        "duration '%s'".formatted(duration),
        leg -> leg.isTransit() && leg.duration().compareTo(duration) < 1);
    return this;
  }

  public ItineraryAssertions withRouteShortName(String... shortNames) {
    addCurrentLegCriterion(
        "route '%s'".formatted(Arrays.toString(shortNames)),
        leg -> {
          String shortName = leg.route().getShortName();
          return leg.isTransit()
              && shortName != null
              && Arrays.asList(shortNames).contains(shortName);
        });
    return this;
  }

  /** Requires the transit leg to board at a stop with one of the supplied stop codes. */
  public ItineraryAssertions withBoardingStopCode(String... stopCodes) {
    addCurrentLegCriterion(
        "boarding stop code '%s'".formatted(Arrays.toString(stopCodes)),
        leg ->
            leg.isTransit()
                && stopCode(leg.from()).map(Arrays.asList(stopCodes)::contains).orElse(false));
    return this;
  }

  /** Requires the transit leg to board at a stop with one of the supplied GTFS IDs. */
  public ItineraryAssertions withBoardingStopGtfsId(String... stopGtfsIds) {
    addCurrentLegCriterion(
        "boarding stop GTFS ID '%s'".formatted(Arrays.toString(stopGtfsIds)),
        leg ->
            leg.isTransit()
                && stopGtfsId(leg.from()).map(Arrays.asList(stopGtfsIds)::contains).orElse(false));
    return this;
  }

  /** Requires the transit leg to alight at a stop with one of the supplied stop codes. */
  public ItineraryAssertions withAlightingStopCode(String... stopCodes) {
    addCurrentLegCriterion(
        "alighting stop code '%s'".formatted(Arrays.toString(stopCodes)),
        leg ->
            leg.isTransit()
                && stopCode(leg.to()).map(Arrays.asList(stopCodes)::contains).orElse(false));
    return this;
  }

  /** Requires the transit leg to alight at a stop with one of the supplied GTFS IDs. */
  public ItineraryAssertions withAlightingStopGtfsId(String... stopGtfsIds) {
    addCurrentLegCriterion(
        "alighting stop GTFS ID '%s'".formatted(Arrays.toString(stopGtfsIds)),
        leg ->
            leg.isTransit()
                && stopGtfsId(leg.to()).map(Arrays.asList(stopGtfsIds)::contains).orElse(false));
    return this;
  }

  public ItineraryAssertions withFarePrice(float price, String riderCategoryId, String mediumId) {
    addCurrentLegCriterion(
        "fare $%.2f (rider category %s, medium %s)".formatted(price, riderCategoryId, mediumId),
        leg ->
            leg.fareProducts().stream()
                .filter(fp -> fp.product().riderCategory().isPresent())
                .filter(fp -> fp.product().medium().isPresent())
                .filter(fp -> fp.product().riderCategory().get().id().equals(riderCategoryId))
                .filter(fp -> fp.product().medium().get().id().equals(mediumId))
                .filter(fp -> fp.product().price() != null)
                .anyMatch(fp -> fp.product().price().amount().floatValue() == price));
    return this;
  }

  /** Requires this leg to be a stay-on-board continuation of the previous transit leg. */
  public ItineraryAssertions interlinedWithPreviousLeg() {
    addCurrentLegCriterion("interlined with previous leg", Leg::interlineWithPreviousLeg);
    return this;
  }

  public ItineraryAssertions withMode(String mode) {
    addCurrentLegCriterion("mode %s".formatted(mode), leg -> leg.mode().toString().equals(mode));
    return this;
  }

  /**
   * Enables strict transit matching. Each expected leg must match the transit leg at the same
   * position, and there must be no additional or missing transit legs.
   */
  public ItineraryAssertions withStrictTransitMatching() {
    this.strictTransitMatching = true;
    return this;
  }

  /** Asserts that at least one itinerary in the given trip plan matches all configured criteria. */
  public void assertMatches(TripPlan tripPlan) {
    validateCriteria();

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
        failuresSection.append(
            "    Leg %d: %s".formatted(legIndex + 1, leg.formatLegDescription()));
      }
      failuresSection.append("%n");
    }

    String fullError = header + criteriaSection + failuresSection;
    throw new ItineraryAssertionError(
        fullError,
        failedResults,
        distinctLegCriteria.stream()
            .map(criteria -> criteria.stream().map(LegCriterion::message).toList())
            .toList(),
        strictTransitMatching,
        tripPlan);
  }

  /** Checks an itinerary using exact positional or unordered matching, as configured. */
  private ItineraryMatchResult matchesAllLegs(Itinerary itinerary) {
    List<Leg> transitLegs = itinerary.legs().stream().filter(Leg::isTransit).toList();

    return strictTransitMatching
        ? matchesExactOrderedLegs(transitLegs)
        : matchesRequiredLegs(transitLegs);
  }

  private ItineraryMatchResult matchesRequiredLegs(List<Leg> transitLegs) {
    List<Leg> remainingLegs = new ArrayList<>(transitLegs);
    List<String> errors = new ArrayList<>();
    List<LegMatchingState> completeMatches = new ArrayList<>();
    List<LegMatchingState> partialMatches = new ArrayList<>();

    for (var criteriaIndex = 0; criteriaIndex < distinctLegCriteria.size(); criteriaIndex++) {
      List<LegCriterion> criteriaSet = distinctLegCriteria.get(criteriaIndex);
      boolean foundMatch = false;

      for (int i = 0; i < remainingLegs.size(); i++) {
        Leg leg = remainingLegs.get(i);
        LegMatchingState state = matchLeg(leg, criteriaSet);

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

    if (errors.isEmpty()) {
      return ItineraryMatchResult.success(completeMatches);
    }

    return new ItineraryMatchResult(completeMatches, partialMatches, List.of(), errors);
  }

  private ItineraryMatchResult matchesExactOrderedLegs(List<Leg> transitLegs) {
    List<String> errors = new ArrayList<>();
    List<LegMatchingState> completeMatches = new ArrayList<>();
    List<LegMatchingState> partialMatches = new ArrayList<>();

    for (int criteriaIndex = 0; criteriaIndex < distinctLegCriteria.size(); criteriaIndex++) {
      List<LegCriterion> criteriaSet = distinctLegCriteria.get(criteriaIndex);

      if (criteriaIndex >= transitLegs.size()) {
        errors.add(
            "No transit leg at position %d matching criteria: %s"
                .formatted(criteriaIndex + 1, describeCriteria(criteriaSet).trim()));
        continue;
      }

      LegMatchingState state = matchLeg(transitLegs.get(criteriaIndex), criteriaSet);
      if (state.isFullMatch()) {
        completeMatches.add(state);
      } else {
        if (state.hasAnyMatch()) {
          partialMatches.add(state);
        }
        errors.add(
            "Transit leg at position %d does not match criteria: %s"
                .formatted(criteriaIndex + 1, describeCriteria(criteriaSet).trim()));
      }
    }

    List<Leg> extraLegs =
        transitLegs.size() > distinctLegCriteria.size()
            ? List.copyOf(transitLegs.subList(distinctLegCriteria.size(), transitLegs.size()))
            : List.of();
    if (!extraLegs.isEmpty()) {
      String extraLegNames =
          extraLegs.stream().map(Leg::routeDisplayName).collect(Collectors.joining(" "));
      errors.add(
          "Itinerary contains additional transit legs when strict matching is enabled: %s"
              .formatted(extraLegNames));
    }

    if (errors.isEmpty()) {
      return ItineraryMatchResult.success(completeMatches);
    }

    return new ItineraryMatchResult(completeMatches, partialMatches, extraLegs, errors);
  }

  private LegMatchingState matchLeg(Leg leg, List<LegCriterion> criteriaSet) {
    LegMatchingState state = new LegMatchingState(leg);
    criteriaSet.forEach(
        criterion -> {
          if (criterion.test().test(leg)) {
            state.addMatch(criterion.message());
          } else {
            state.addFailure(criterion.message());
          }
        });
    return state;
  }

  private void validateCriteria() {
    if (distinctLegCriteria.isEmpty()) {
      throw new IllegalArgumentException("No leg criteria specified");
    }

    for (int i = 0; i < distinctLegCriteria.size(); i++) {
      if (distinctLegCriteria.get(i).isEmpty()) {
        throw new IllegalArgumentException("No leg criteria specified for criteria set " + (i + 1));
      }
    }
  }

  private static Optional<String> stopCode(Place place) {
    return place.stop().flatMap(stop -> stop.code());
  }

  private static Optional<String> stopGtfsId(Place place) {
    return place.stop().map(stop -> stop.id());
  }
}
