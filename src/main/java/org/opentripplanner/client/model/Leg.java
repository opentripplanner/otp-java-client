package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

public record Leg(
    Place from,
    Place to,
    OffsetDateTime startTime,
    OffsetDateTime endTime,
    LegMode mode,
    Duration duration,
    double distance,
    Optional<String> headsign,
    Route route,
    Trip trip,
    List<FareProductUse> fareProducts,
    OptionalDouble accessibilityScore,
    Agency agency,
    LegGeometry legGeometry,
    @JsonProperty("rentedBike")
    boolean rentedVehicle,
    Optional<List<IntermediatePlace>> intermediatePlaces
    ) {

  /** Is this leg using public transport? */
  public boolean isTransit() {
    return mode.isTransit();
  }
}
