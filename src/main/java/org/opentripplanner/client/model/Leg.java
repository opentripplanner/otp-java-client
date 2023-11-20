package org.opentripplanner.client.model;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.OptionalDouble;
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
    List<FareProductUse> fareProducts,
    OptionalDouble accessibilityScore) {

  /** Is this leg using public transport? */
  public boolean isTransit() {
    return mode.isTransit();
  }
}
