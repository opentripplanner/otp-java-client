package org.opentripplanner.client.model;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.opentripplanner.client.model.TripPlan.Place;

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

  /**
   * Is this leg using public transport?
   */
  public boolean isTransit() {
    return mode.isTransit();
  }
}
