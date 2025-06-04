package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VehicleRentalStation(
    @JsonProperty("stationId") String id, String name, float lat, float lon, String network) {}
