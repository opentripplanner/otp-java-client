package org.opentripplanner.assertions;

import java.util.List;
import org.opentripplanner.client.model.TripPlan;

public class ItineraryAssertionError extends AssertionError {
  private final List<ItineraryMatchResult> failedResults;
  private final List<List<String>> expectedLegs;
  private final boolean strictTransitMatching;
  private final TripPlan tripPlan;

  /** Captures reportable expectations without exposing executable predicates. */
  public ItineraryAssertionError(
      String message,
      List<ItineraryMatchResult> failedResults,
      List<List<String>> expectedLegs,
      boolean strictTransitMatching,
      TripPlan tripPlan) {
    super(message);
    this.failedResults = List.copyOf(failedResults);
    this.expectedLegs = expectedLegs.stream().map(List::copyOf).toList();
    this.strictTransitMatching = strictTransitMatching;
    this.tripPlan = tripPlan;
  }

  public List<ItineraryMatchResult> getFailedResults() {
    return failedResults;
  }

  public List<List<String>> getExpectedLegs() {
    return expectedLegs;
  }

  public boolean isStrictTransitMatching() {
    return strictTransitMatching;
  }

  /** The response used by the failed assertion */
  public TripPlan getTripPlan() {
    return tripPlan;
  }
}
