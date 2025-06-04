package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;

public record Trip(
    @JsonProperty("gtfsId") String id,
    @JsonProperty("tripShortName") Optional<String> shortName,
    @JsonProperty("tripHeadsign") Optional<String> headsign) {
  public Trip {
    Objects.requireNonNull(id);
  }
}
