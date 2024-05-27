package org.opentripplanner.client.model;

public enum RequestMode {
  TRANSIT("TRANSIT"),
  WALK("WALK"),
  RAIL("RAIL"),
  BUS("BUS"),
  SUBWAY("SUBWAY"),
  TRAM("TRAM"),
  FERRY("FERRY"),
  CAR("CAR"),
  CAR_PARK("CAR", "PARK"),
  SCOOTER_RENT("SCOOTER", "RENT"),
  BICYCLE("BICYCLE"),
  BICYCLE_PARK("BICYCLE", "PARK"),
  BICYCLE_RENT("BICYCLE", "RENT"),
  FLEX_DIRECT("FLEX", "DIRECT"),
  FLEX_ACCESS("FLEX", "ACCESS"),
  FLEX_EGRESS("FLEX", "EGRESS");

  public final String mode;
  public final String qualifier;

  RequestMode(String mode) {
    this(mode, null);
  }

  RequestMode(String mode, String qualifier) {
    this.mode = mode;
    this.qualifier = qualifier;
  }
}
