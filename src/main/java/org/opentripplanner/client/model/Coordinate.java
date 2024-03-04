package org.opentripplanner.client.model;

public record Coordinate(double lat, double lon) implements PlaceParameter {

  @Override
  public String toString() {
    return "%s,%s".formatted(lat, lon);
  }

  @Override
  public String toPlanParameter(String direction) {
    return String.format("%s: {lat: %s, lon: %s}", direction, lat(), lon());
  }
}
