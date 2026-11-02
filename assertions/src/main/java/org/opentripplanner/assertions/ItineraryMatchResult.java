package org.opentripplanner.assertions;

import java.util.List;
import org.opentripplanner.client.model.Itinerary;
import org.opentripplanner.client.model.Leg;

/**
 * Result of matching one itinerary against all configured leg criteria.
 *
 * @param itineraryIndex Zero-based index in the original trip plan
 * @param itinerary The evaluated itinerary, with a snapshot of its leg list
 * @param legResults One outcome per expected leg, in expectation order
 * @param extraMatches Transit legs unmatched in strict matching mode
 * @param errors Itinerary-level errors, such as exceeding the maximum duration
 */
public record ItineraryMatchResult(
    int itineraryIndex,
    Itinerary itinerary,
    List<LegRequirementResult> legResults,
    List<Leg> extraMatches,
    List<String> errors) {
  public ItineraryMatchResult {
    itinerary = new Itinerary(List.copyOf(itinerary.legs()), itinerary.accessibilityScore());
    legResults = List.copyOf(legResults);
    extraMatches = List.copyOf(extraMatches);
    errors = List.copyOf(errors);
  }

  /**
   * @return Whether this result represents a successful match.
   */
  public boolean isSuccess() {
    return errors.isEmpty()
        && extraMatches.isEmpty()
        && legResults.stream().allMatch(LegRequirementResult::isSuccess);
  }
}
