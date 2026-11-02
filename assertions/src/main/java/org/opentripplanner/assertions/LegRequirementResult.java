package org.opentripplanner.assertions;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Outcome for one expected leg, retaining its criteria and actual candidate legs.
 *
 * @param expectedLegIndex Zero-based index among the expected legs
 * @param expectedCriteria Descriptions of all criteria for this requirement
 * @param status Whether the requirement matched, or why it could not be assigned a leg
 * @param matchedLeg The assigned full match; present exactly when status is MATCHED
 * @param candidates All evaluations considered by the strategy, including the assigned match and
 *     full, partial, and nonmatching candidates, regardless of success. Satisfying the criteria
 *     does not guarantee that a candidate can be assigned without conflicts or order violations.
 */
public record LegRequirementResult(
    int expectedLegIndex,
    List<String> expectedCriteria,
    Status status,
    Optional<LegMatchResult> matchedLeg,
    List<LegMatchResult> candidates) {
  public enum Status {
    MATCHED,
    CRITERIA_MISMATCH,
    MISSING_LEG,
    ALREADY_ASSIGNED,
    OUT_OF_ORDER
  }

  public LegRequirementResult {
    expectedCriteria = List.copyOf(expectedCriteria);
    Objects.requireNonNull(status);
    Objects.requireNonNull(matchedLeg);
    candidates = List.copyOf(candidates);
    if (matchedLeg.isPresent() != (status == Status.MATCHED)) {
      throw new IllegalArgumentException(
          "An assigned leg must be present exactly when status is MATCHED");
    }
    if (matchedLeg.isPresent()) {
      var assigned = matchedLeg.orElseThrow();
      if (!assigned.isFullMatch() || !candidates.contains(assigned)) {
        throw new IllegalArgumentException(
            "The assigned leg must fully match and be included in candidates");
      }
    }
  }

  public boolean isSuccess() {
    return status == Status.MATCHED;
  }
}
