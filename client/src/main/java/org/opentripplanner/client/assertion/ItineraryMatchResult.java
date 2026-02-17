package org.opentripplanner.client.assertion;

import java.util.List;
import org.opentripplanner.client.model.Leg;

/**
 * Result of matching one itinerary against all configured leg criteria.
 *
 * @param successfulMatches Legs fully matching each required criterion set
 * @param partialMatches Legs matching some but not all criteria
 * @param extraMatches Transit legs unmatched in strict matching mode
 * @param errors Matching errors
 */
public record ItineraryMatchResult(
    List<LegMatchingState> successfulMatches,
    List<LegMatchingState> partialMatches,
    List<Leg> extraMatches,
    List<String> errors) {
  public static ItineraryMatchResult success(List<LegMatchingState> successfulMatches) {
    return new ItineraryMatchResult(successfulMatches, List.of(), List.of(), List.of());
  }

  /**
   * @return Whether this result represents a successful match.
   */
  public boolean isSuccess() {
    return !successfulMatches.isEmpty() && extraMatches.isEmpty() && partialMatches().isEmpty();
  }

  public List<LegMatchingState> getPartialMatches() {
    return partialMatches();
  }
}
