package org.opentripplanner.assertions;

import java.util.Arrays;
import java.util.List;
import org.opentripplanner.client.model.TripPlan;

/** Defines an expectation for a TripPlan and provides factories for composing expectations. */
@FunctionalInterface
public interface PlanExpectation {
  PlanMatchResult evaluate(TripPlan plan);

  static PlanExpectation anyOf(PlanExpectation... alternatives) {
    return composite(true, alternatives);
  }

  static PlanExpectation allOf(PlanExpectation... requirements) {
    return composite(false, requirements);
  }

  private static PlanExpectation composite(boolean any, PlanExpectation[] expectations) {
    var copy = List.copyOf(Arrays.asList(expectations));
    if (copy.isEmpty()) throw new IllegalArgumentException("At least one expectation is required");
    return plan -> {
      var results = copy.stream().map(e -> e.evaluate(plan)).toList();
      boolean matches =
          any
              ? results.stream().anyMatch(PlanMatchResult::matches)
              : results.stream().allMatch(PlanMatchResult::matches);
      return result(
          any ? "Any expectation must match" : "All expectations must match",
          matches,
          plan,
          results);
    };
  }

  static PlanExpectation itineraryCountBetween(int minimum, int maximum) {
    if (minimum < 0 || maximum < minimum)
      throw new IllegalArgumentException("Invalid itinerary count range");
    return plan -> {
      int actual = plan.itineraries().size();
      return result(
          "Itinerary count " + actual + "; expected " + minimum + ".." + maximum,
          actual >= minimum && actual <= maximum,
          plan,
          List.of());
    };
  }

  private static PlanMatchResult result(
      String description, boolean matches, TripPlan plan, List<PlanMatchResult> children) {
    return new PlanMatchResult(
        description, matches, plan, null, List.of(), List.of(), List.of(), children);
  }
}
