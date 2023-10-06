package org.opentripplanner.client.model;

import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

public record TripPlan(List<Itinerary> itineraries) {

  public record Itinerary(List<Leg> legs) {

    /** Does this itinerary contain any legs that contain public transport? */
    public boolean hasTransit() {
      return legs.stream().anyMatch(Leg::isTransit);
    }

    /**
     * @return All legs that are using public transport.
     */
    public List<Leg> transitLegs() {
      return legs.stream().filter(Leg::isTransit).toList();
    }
  }

  public record Place(String name) {}

  public record Route(String shortName, String longName, Agency agency) {}

  public record Agency(String name) {}

  public record Currency(int digits, String code) {}

  public record Money(BigDecimal amount, Currency currency) {}

  public record RiderCategory(String id, String name) {}

  public record FareMedium(String id, String name) {}

  public record FareProduct(
      String id,
      String name,
      Money price,
      @Nullable RiderCategory riderCategory,
      @Nullable FareMedium medium) {}

  public record FareProductUse(String id, FareProduct product) {}

  public record Leg(
      Place from,
      Place to,
      OffsetDateTime startTime,
      OffsetDateTime endTime,
      LegMode mode,
      Duration duration,
      double distance,
      Route route,
      List<FareProductUse> fareProducts) {

    /** Is this leg using public transport? */
    public boolean isTransit() {
      return mode.isTransit();
    }
  }

  /** Returns a list of all itineraries that contain public transport. */
  public List<Itinerary> transitItineraries() {
    return itineraries.stream().filter(Itinerary::hasTransit).toList();
  }
}
