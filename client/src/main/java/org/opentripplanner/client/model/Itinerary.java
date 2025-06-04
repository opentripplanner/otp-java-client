package org.opentripplanner.client.model;

import java.util.List;
import java.util.OptionalDouble;

public record Itinerary(List<Leg> legs, OptionalDouble accessibilityScore) {

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
