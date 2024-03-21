package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public record Trip(
    @JsonProperty("gtfsId") String id,
    Optional<String> tripShortName,
    Optional<String> tripHeadsign) {}
