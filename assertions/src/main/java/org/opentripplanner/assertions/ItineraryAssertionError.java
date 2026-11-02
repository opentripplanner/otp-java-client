package org.opentripplanner.assertions;

public final class ItineraryAssertionError extends AssertionError {
  private final PlanMatchResult result;

  public ItineraryAssertionError(PlanMatchResult result) {
    super(MatchResultFormatter.format(result));
    this.result = result;
  }

  public PlanMatchResult result() {
    return result;
  }
}
