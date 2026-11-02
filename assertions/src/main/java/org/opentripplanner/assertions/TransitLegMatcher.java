package org.opentripplanner.assertions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.opentripplanner.assertions.LegRequirementResult.Status;
import org.opentripplanner.client.model.Itinerary;
import org.opentripplanner.client.model.Leg;

/**
 * Assigns expected leg requirements to distinct actual transit legs, then records their outcomes.
 * Every strategy produces one result per requirement; itinerary success requires all of them to
 * succeed. The strategy determines which assignments are allowed.
 *
 * <p>All indices are zero-based. Actual-leg indices refer to the transit-only list, so walking and
 * other non-transit legs do not affect positions or ordering.
 */
final class TransitLegMatcher {
  private final List<List<LegCriterion>> criteria;
  private final int itineraryIndex;
  private final Itinerary itinerary;
  private final List<Leg> actual;
  private final LegMatchResult[][] states;

  /**
   * Builds states[expected requirement][actual transit leg], evaluating every criterion once for
   * each pair. Assignment searches reuse these immutable evaluations, including when an unordered
   * search moves a requirement to another leg.
   */
  TransitLegMatcher(List<List<LegCriterion>> criteria, int itineraryIndex, Itinerary itinerary) {
    this.criteria = criteria;
    this.itineraryIndex = itineraryIndex;
    this.itinerary = itinerary;
    this.actual = itinerary.transitLegs();
    states = new LegMatchResult[criteria.size()][actual.size()];
    for (int e = 0; e < criteria.size(); e++) {
      for (int a = 0; a < actual.size(); a++)
        states[e][a] = evaluate(a, actual.get(a), criteria.get(e));
    }
  }

  /**
   * Applies the selected assignment rules and returns structured results. A candidate satisfying
   * all criteria becomes MATCHED only if the strategy can assign it to that requirement.
   */
  ItineraryMatchResult match(TransitLegMatching mode) {
    return switch (mode) {
      case EXACT_ORDERED -> exact();
      case ORDERED_SUBSEQUENCE -> generateMatchResult(ordered(), mode);
      case UNORDERED_SUBSET -> generateMatchResult(unordered(), mode);
    };
  }

  /**
   * Compares requirements and transit legs at identical positions. Missing positions produce failed
   * requirements; actual legs beyond the expected count are retained as extras, which also prevent
   * itinerary success.
   */
  private ItineraryMatchResult exact() {
    var results = new ArrayList<LegRequirementResult>();
    for (int e = 0; e < criteria.size(); e++) results.add(positional(e));
    var extras =
        actual.size() > criteria.size()
            ? actual.subList(criteria.size(), actual.size())
            : List.<Leg>of();
    return new ItineraryMatchResult(itineraryIndex, itinerary, results, extras, List.of());
  }

  /**
   * Evaluates one exact-position requirement. Retains the positional candidate even when none of
   * its criteria match; if the itinerary is too short, there is no candidate to report.
   */
  private LegRequirementResult positional(int e) {
    if (e >= actual.size()) {
      return requirement(e, Status.MISSING_LEG, Optional.empty(), List.of());
    }
    var state = states[e][e];
    if (state.isFullMatch()) {
      return requirement(e, Status.MATCHED, Optional.of(state), List.of(state));
    }
    return requirement(e, Status.CRITERIA_MISMATCH, Optional.empty(), List.of(state));
  }

  /**
   * Builds assignment[expected] = actual for an ordered subsequence. Choosing the earliest
   * available full match leaves the largest suffix for later requirements. Advancing past each
   * match prevents reuse and preserves travel order, while allowing extra transit legs between
   * matches.
   *
   * <p>A requirement with no remaining match keeps -1; later requirements are still evaluated so
   * the result includes their diagnostics too.
   */
  private int[] ordered() {
    int[] assignment = emptyAssignment(criteria.size());
    // First allowed actual-leg index for the next requirement; advances only after a match.
    int next = 0;
    for (int e = 0; e < criteria.size(); e++) {
      int candidate = nextMatch(e, next);
      if (candidate >= 0) {
        assignment[e] = candidate;
        next = candidate + 1;
      }
    }
    return assignment;
  }

  /**
   * Searches the cached evaluations for this requirement, starting at the inclusive actual-leg
   * index. Returns -1 if no leg in the remaining suffix satisfies every criterion.
   */
  private int nextMatch(int expected, int start) {
    for (int a = start; a < actual.size(); a++) if (states[expected][a].isFullMatch()) return a;
    return -1;
  }

  /**
   * Finds a maximum distinct assignment without enforcing order. owner[actual] = expected tracks
   * the requirement currently using each leg. Augmenting paths can move earlier requirements to
   * other matching legs so an overlapping requirement is not rejected just because of an earlier
   * choice.
   *
   * <p>Converts the final ownership map to assignment[expected] = actual, with -1 for unassigned
   * requirements. See <a
   * href="https://cp-algorithms.com/graph/kuhn_maximum_bipartite_matching.html">bipartite
   * matching</a>.
   */
  private int[] unordered() {
    int[] owner = emptyAssignment(actual.size());
    for (int e = 0; e < criteria.size(); e++) augment(e, owner, new boolean[actual.size()]);
    int[] assignment = emptyAssignment(criteria.size());
    for (int a = 0; a < owner.length; a++) if (owner[a] >= 0) assignment[owner[a]] = a;
    return assignment;
  }

  /**
   * Tries to assign one requirement, recursively moving a candidate's current owner if necessary.
   * For example, a BUS requirement can move from route 10 to route 20 so a route-10-only
   * requirement can take route 10.
   *
   * <p>The visited array prevents cycles within this search; unordered starts a fresh array for
   * each new requirement. Ownership changes only along a successful reassignment path, so a failed
   * search preserves the existing assignment.
   */
  private boolean augment(int expected, int[] owner, boolean[] visited) {
    for (int a = 0; a < actual.size(); a++) {
      if (visited[a] || !states[expected][a].isFullMatch()) continue;
      visited[a] = true;
      // Claim this leg only if it is free or its current owner can move to another leg.
      if (owner[a] < 0 || augment(owner[a], owner, visited)) {
        owner[a] = expected;
        return true;
      }
    }
    return false;
  }

  /**
   * Converts a completed subset assignment into one result per requirement. Every result retains
   * all candidate evaluations, including full matches, partial matches, and candidates matching no
   * criteria. The optional matchedLeg separately identifies the leg actually assigned.
   *
   * <p>Both subset strategies allow extra actual legs, so they do not populate extraMatches.
   */
  private ItineraryMatchResult generateMatchResult(int[] assignment, TransitLegMatching mode) {
    var results = new ArrayList<LegRequirementResult>();
    boolean[] assigned = new boolean[actual.size()];
    // Use the complete assignment when distinguishing order failures from occupied candidates.
    Arrays.stream(assignment).filter(a -> a >= 0).forEach(a -> assigned[a] = true);
    for (int e = 0; e < assignment.length; e++) {
      var candidates = Arrays.stream(states[e]).toList();
      if (assignment[e] >= 0) {
        results.add(
            requirement(e, Status.MATCHED, Optional.of(states[e][assignment[e]]), candidates));
      } else {
        results.add(
            requirement(
                e, failureStatus(candidates, assigned, mode), Optional.empty(), candidates));
      }
    }
    return new ItineraryMatchResult(itineraryIndex, itinerary, results, List.of(), List.of());
  }

  /**
   * Explains why an unassigned subset requirement failed: no actual legs, no full criterion match,
   * or full matches that could not be used. In ordered mode, an unused full match was blocked by
   * travel order; otherwise all full matches are already assigned to other requirements.
   */
  private Status failureStatus(
      List<LegMatchResult> candidates, boolean[] assigned, TransitLegMatching mode) {
    if (actual.isEmpty()) return Status.MISSING_LEG;
    var fullMatches = candidates.stream().filter(LegMatchResult::isFullMatch).toList();
    if (fullMatches.isEmpty()) return Status.CRITERIA_MISMATCH;
    if (mode == TransitLegMatching.ORDERED_SUBSEQUENCE
        && fullMatches.stream().anyMatch(candidate -> !assigned[candidate.actualLegIndex()])) {
      return Status.OUT_OF_ORDER;
    }
    return Status.ALREADY_ASSIGNED;
  }

  /**
   * Attaches the original expectation index and criterion descriptions to an assignment outcome,
   * keeping diagnostics associated with the requirement even when several requirements overlap.
   */
  private LegRequirementResult requirement(
      int expectedIndex,
      Status status,
      Optional<LegMatchResult> matchedLeg,
      List<LegMatchResult> candidates) {
    return new LegRequirementResult(
        expectedIndex,
        criteria.get(expectedIndex).stream().map(LegCriterion::message).toList(),
        status,
        matchedLeg,
        candidates);
  }

  /**
   * Creates an assignment or ownership map whose entries are all unassigned. The -1 sentinel leaves
   * index 0 available as a valid requirement or transit-leg index.
   */
  private static int[] emptyAssignment(int size) {
    int[] assignment = new int[size];
    Arrays.fill(assignment, -1);
    return assignment;
  }

  /**
   * Evaluates every criterion for one requirement-leg pair and partitions the descriptions into
   * satisfied and failed lists. This records criterion satisfaction; whether the leg can be
   * assigned to the requirement is decided separately by the matching strategy.
   */
  private static LegMatchResult evaluate(int actualIndex, Leg leg, List<LegCriterion> criteria) {
    var matching = new ArrayList<String>();
    var missing = new ArrayList<String>();
    for (var criterion : criteria) {
      if (criterion.test().test(leg)) matching.add(criterion.message());
      else missing.add(criterion.message());
    }
    return new LegMatchResult(actualIndex, leg, matching, missing);
  }
}
