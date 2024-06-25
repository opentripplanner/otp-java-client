package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public record RentalVehicle(
    @JsonProperty("vehicleId") String id,
    float lon,
    float lat,
    Optional<String> name,
    String network,
    Optional<RentalVehicleType> vehicleType)
    implements Place {}
