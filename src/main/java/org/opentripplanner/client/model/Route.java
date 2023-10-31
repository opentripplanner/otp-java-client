package org.opentripplanner.client.model;

import java.util.Optional;

public record Route(
    Optional<String> shortName, Optional<String> longName, TransitMode mode, Agency agency) {

  /**
   * Either the short name (if it has one) or the long name.
   *
   * <p>Either is optional but one must exist.
   */
  public String name() {
    return shortName.or(() -> longName).orElseThrow();
  }
}
