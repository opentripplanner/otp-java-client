package org.opentripplanner.client.assertion;

import java.util.List;

public class ItineraryAssertionError extends AssertionError {

  private final List<ItineraryMatchResult> failedResults;

  public ItineraryAssertionError(String message, List<ItineraryMatchResult> failedResults) {
    super(message);
    this.failedResults = failedResults;
  }

  public List<ItineraryMatchResult> getFailedResults() {
    return failedResults;
  }
}
