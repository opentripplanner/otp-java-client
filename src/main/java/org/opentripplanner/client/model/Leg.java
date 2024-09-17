package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import org.opentripplanner.client.serialization.PlaceDeserializer;

public record Leg(
    @JsonDeserialize(using = PlaceDeserializer.class) Place from,
    @JsonDeserialize(using = PlaceDeserializer.class) Place to,
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
    @JsonProperty("legGeometry") LegGeometry geometry,
    @JsonProperty("rentedBike") boolean rentedVehicle,
    Optional<List<IntermediatePlace>> intermediatePlaces) {

  /** Is this leg using public transport? */
  public boolean isTransit() {
    return mode.isTransit();
  }
}
