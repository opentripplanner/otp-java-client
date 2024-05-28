package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public record VehicleParking(
        @JsonProperty("vehicleParkingId") String id,
        String name, Optional<ParkingCapacity> capacity
) { }
