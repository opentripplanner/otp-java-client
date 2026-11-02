package org.opentripplanner.assertions;

import java.util.List;
import java.util.Objects;
import org.opentripplanner.client.model.Leg;

/**
 * Immutable evaluation of one actual transit leg against one requirement.
 *
 * @param actualLegIndex Zero-based index among the itinerary's transit legs, excluding walking
 * @param leg The actual leg evaluated
 * @param matchingCriteria Descriptions of satisfied criteria
 * @param missingCriteria Descriptions of failed criteria
 */
public record LegMatchResult(
    int actualLegIndex, Leg leg, List<String> matchingCriteria, List<String> missingCriteria) {
  public LegMatchResult {
    Objects.requireNonNull(leg);
    matchingCriteria = List.copyOf(matchingCriteria);
    missingCriteria = List.copyOf(missingCriteria);
  }

  public boolean isFullMatch() {
    return missingCriteria.isEmpty() && !matchingCriteria.isEmpty();
  }

  /** Whether at least one criterion matched and at least one criterion failed. */
  public boolean isPartialMatch() {
    return !matchingCriteria.isEmpty() && !missingCriteria.isEmpty();
  }
}
