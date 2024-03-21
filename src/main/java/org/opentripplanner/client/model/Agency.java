package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record Agency(@JsonProperty("gtfsId") String id, String name) {
  public Agency {
    Objects.requireNonNull(id);
    Objects.requireNonNull(name);
  }

}
