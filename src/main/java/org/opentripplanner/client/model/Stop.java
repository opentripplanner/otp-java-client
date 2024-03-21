package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;

public record Stop(String name, @JsonProperty("gtfsId") String id, Optional<String> code) {
  public Stop {
    Objects.requireNonNull(name);
    Objects.requireNonNull(id);
  }
}
