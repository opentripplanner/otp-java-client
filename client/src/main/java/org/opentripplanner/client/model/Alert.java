package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public record Alert(
    @JsonProperty("alertHeaderText") String header,
    @JsonProperty("alertDescriptionText") String description) {
  public Alert {
    Objects.requireNonNull(header);
    Objects.requireNonNull(description);
  }
}
