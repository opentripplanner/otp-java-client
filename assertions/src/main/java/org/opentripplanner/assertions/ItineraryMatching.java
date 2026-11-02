package org.opentripplanner.assertions;

import java.util.List;

/** Determines how many itineraries in a plan must satisfy the itinerary conditions. */
public enum ItineraryMatching {
  /** At least one itinerary must match. Fails for an empty plan. */
  ANY("At least one itinerary must match"),
  /** Every itinerary must match, and the plan must contain at least one itinerary. */
  ALL("Every itinerary must match"),
  /** No itinerary may match. Succeeds for an empty plan. */
  NONE("No itinerary must match");

  private final String description;

  ItineraryMatching(String description) {
    this.description = description;
  }

  String description() {
    return description;
  }

  boolean matches(List<ItineraryMatchResult> results) {
    return switch (this) {
      case ANY -> results.stream().anyMatch(ItineraryMatchResult::isSuccess);
      case ALL -> !results.isEmpty() && results.stream().allMatch(ItineraryMatchResult::isSuccess);
      case NONE -> results.stream().noneMatch(ItineraryMatchResult::isSuccess);
    };
  }
}
