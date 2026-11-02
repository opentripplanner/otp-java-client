package org.opentripplanner.assertions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.opentripplanner.client.model.Place;
import org.opentripplanner.client.model.Stop;

/** Conditions on a single transit leg; all configured conditions must hold. */
public final class TransitLegExpectation {
  private final List<LegCriterion> criteria = new ArrayList<>();

  public TransitLegExpectation withRouteLongName(String... longNames) {
    var candidates = candidates(longNames);
    criteria.add(
        new LegCriterion(
            "route '%s'".formatted(candidates.toString()),
            leg -> {
              String longName = leg.route().getLongName();
              return leg.isTransit() && longName != null && candidates.contains(longName);
            }));
    return this;
  }

  public TransitLegExpectation withMaxDuration(Duration duration) {
    requireDuration(duration);
    criteria.add(
        new LegCriterion(
            "duration '%s'".formatted(duration),
            leg -> leg.isTransit() && leg.duration().compareTo(duration) < 1));
    return this;
  }

  public TransitLegExpectation withRouteShortName(String... shortNames) {
    var candidates = candidates(shortNames);
    criteria.add(
        new LegCriterion(
            "route '%s'".formatted(candidates.toString()),
            leg -> {
              String shortName = leg.route().getShortName();
              return leg.isTransit() && shortName != null && candidates.contains(shortName);
            }));
    return this;
  }

  /** Requires the transit leg to board at a stop with one of the supplied stop codes. */
  public TransitLegExpectation withBoardingStopCode(String... stopCodes) {
    var candidates = candidates(stopCodes);
    criteria.add(
        new LegCriterion(
            "boarding stop code '%s'".formatted(candidates.toString()),
            leg ->
                leg.isTransit() && stopCode(leg.from()).map(candidates::contains).orElse(false)));
    return this;
  }

  /** Requires the transit leg to board at a stop with one of the supplied GTFS IDs. */
  public TransitLegExpectation withBoardingStopGtfsId(String... stopGtfsIds) {
    var candidates = candidates(stopGtfsIds);
    criteria.add(
        new LegCriterion(
            "boarding stop GTFS ID '%s'".formatted(candidates.toString()),
            leg ->
                leg.isTransit() && stopGtfsId(leg.from()).map(candidates::contains).orElse(false)));
    return this;
  }

  /** Requires the transit leg to alight at a stop with one of the supplied stop codes. */
  public TransitLegExpectation withAlightingStopCode(String... stopCodes) {
    var candidates = candidates(stopCodes);
    criteria.add(
        new LegCriterion(
            "alighting stop code '%s'".formatted(candidates.toString()),
            leg -> leg.isTransit() && stopCode(leg.to()).map(candidates::contains).orElse(false)));
    return this;
  }

  /** Requires the transit leg to alight at a stop with one of the supplied GTFS IDs. */
  public TransitLegExpectation withAlightingStopGtfsId(String... stopGtfsIds) {
    var candidates = candidates(stopGtfsIds);
    criteria.add(
        new LegCriterion(
            "alighting stop GTFS ID '%s'".formatted(candidates.toString()),
            leg ->
                leg.isTransit() && stopGtfsId(leg.to()).map(candidates::contains).orElse(false)));
    return this;
  }

  public TransitLegExpectation withFarePrice(float price, String riderCategoryId, String mediumId) {
    if (!Float.isFinite(price) || price < 0)
      throw new IllegalArgumentException("Fare must be finite and nonnegative");
    criteria.add(
        new LegCriterion(
            "fare $%.2f (rider category %s, medium %s)".formatted(price, riderCategoryId, mediumId),
            leg ->
                leg.fareProducts().stream()
                    .filter(fp -> fp.product().riderCategory().isPresent())
                    .filter(fp -> fp.product().medium().isPresent())
                    .filter(fp -> fp.product().riderCategory().get().id().equals(riderCategoryId))
                    .filter(fp -> fp.product().medium().get().id().equals(mediumId))
                    .filter(fp -> fp.product().price() != null)
                    .anyMatch(fp -> fp.product().price().amount().floatValue() == price)));
    return this;
  }

  /** Requires this leg to be a stay-on-board continuation of the previous transit leg. */
  public TransitLegExpectation withInterlinedWithPreviousLeg(boolean required) {
    criteria.add(
        new LegCriterion(
            "interlined with previous leg = " + required,
            leg -> leg.interlineWithPreviousLeg() == required));
    return this;
  }

  public TransitLegExpectation withMode(String mode) {
    var parsed = org.opentripplanner.client.model.LegMode.valueOf(mode);
    if (!parsed.isTransit()) throw new IllegalArgumentException("Expected a transit mode: " + mode);
    criteria.add(
        new LegCriterion("mode %s".formatted(mode), leg -> leg.mode().toString().equals(mode)));
    return this;
  }

  /** Used to validate the input values to the expectation methods */
  private static List<String> candidates(String[] values) {
    var candidates = List.copyOf(Arrays.asList(values));
    if (candidates.isEmpty() || candidates.stream().anyMatch(String::isBlank)) {
      throw new IllegalArgumentException("At least one nonblank candidate is required");
    }
    return candidates;
  }

  List<LegCriterion> criteria() {
    if (criteria.isEmpty()) throw new IllegalArgumentException("No leg criteria specified");
    return List.copyOf(criteria);
  }

  static void requireDuration(Duration duration) {
    if (duration == null || duration.isNegative()) {
      throw new IllegalArgumentException("Duration must be nonnegative");
    }
  }

  private static Optional<String> stopCode(Place place) {
    return place.stop().flatMap(Stop::code);
  }

  private static Optional<String> stopGtfsId(Place place) {
    return place.stop().map(Stop::id);
  }
}
