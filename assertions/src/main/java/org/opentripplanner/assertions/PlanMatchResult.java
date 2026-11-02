package org.opentripplanner.assertions;

import java.util.List;
import java.util.Objects;
import org.opentripplanner.client.model.Itinerary;
import org.opentripplanner.client.model.TripPlan;

/**
 * An evaluation snapshot, including successful matches and all alternative diagnostics. Copies the
 * plan's itinerary and leg lists; individual client-model values are retained.
 *
 * @param description Human-readable summary of the expectation being evaluated, rather than its
 *     outcome or the full diagnostic report
 * @param matches Whether this entire expectation succeeded according to its own rules, such as at
 *     least one itinerary matching, every child expectation matching, or an itinerary count limit
 * @param plan The evaluated trip plan, with snapshots of its itinerary and leg lists
 * @param matchingMode Strategy used to assign expected legs to actual transit legs; null when this
 *     result represents a plan-only check or composition rather than a direct itinerary evaluation
 * @param expectedLegs Criterion descriptions grouped by expected leg, in declaration order. Every
 *     criterion in an inner list must hold on the same leg; matchingMode determines whether leg
 *     order matters. Empty when no legs are required or this result delegates to children.
 * @param itineraryCriteria Descriptions of conditions on an entire itinerary, such as its maximum
 *     duration. These describe the expectations, not evaluation failures; empty when none apply.
 * @param itineraryResults Results for every evaluated itinerary, including successes and failures,
 *     in plan order. Empty for plan-only checks and results that delegate evaluation to children.
 * @param children Results of nested expectations, in declaration order, retaining both successful
 *     and failed alternatives. Used by composition; empty for direct itinerary and itinerary-count
 *     checks. Each child's matches flag describes that child's own expectation.
 */
public record PlanMatchResult(
    String description,
    boolean matches,
    TripPlan plan,
    TransitLegMatching matchingMode,
    List<List<String>> expectedLegs,
    List<String> itineraryCriteria,
    List<ItineraryMatchResult> itineraryResults,
    List<PlanMatchResult> children) {
  public PlanMatchResult {
    Objects.requireNonNull(description);
    Objects.requireNonNull(plan);
    plan =
        new TripPlan(
            plan.itineraries().stream()
                .map(i -> new Itinerary(List.copyOf(i.legs()), i.accessibilityScore()))
                .toList(),
            plan.nextPageCursor(),
            plan.previousPageCursor());
    expectedLegs = expectedLegs.stream().map(List::copyOf).toList();
    itineraryCriteria = List.copyOf(itineraryCriteria);
    itineraryResults = List.copyOf(itineraryResults);
    children = List.copyOf(children);
  }

  public void assertMatches() {
    if (!matches) throw new ItineraryAssertionError(this);
  }
}
