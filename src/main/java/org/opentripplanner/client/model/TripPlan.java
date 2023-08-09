package org.opentripplanner.client.model;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

public record TripPlan(List<Itinerary> itineraries) {
  public record Itinerary(List<Leg> legs) {}

  public record Place(String name) {}

  public record Route(String shortName, String longName, Agency agency) {}
  ;

  public record Agency(String name) {}
  ;

  public record Leg(
      Place from,
      Place to,
      OffsetDateTime startTime,
      OffsetDateTime endTime,
      Mode mode,
      Duration duration,
      double distance,
      Route route) {}
  ;
}
