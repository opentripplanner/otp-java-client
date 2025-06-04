package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public record Route(
    @JsonProperty("gtfsId") String id,
    Optional<String> shortName,
    Optional<String> longName,
    @JsonProperty("type") int modeCode,
    TransitMode mode,
    Agency agency) {

  /**
   * Either the short name (if it has one) or the long name.
   *
   * <p>Either is optional but one must exist.
   */
  public String name() {
    return shortName.or(() -> longName).orElseThrow();
  }
}
