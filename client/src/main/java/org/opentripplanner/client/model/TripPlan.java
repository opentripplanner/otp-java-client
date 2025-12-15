package org.opentripplanner.client.model;

import java.util.List;

public record TripPlan(
    List<Itinerary> itineraries, String nextPageCursor, String previousPageCursor) {

  /** Returns a list of all itineraries that contain public transport. */
  public List<Itinerary> transitItineraries() {
    return itineraries.stream().filter(Itinerary::hasTransit).toList();
  }
}
