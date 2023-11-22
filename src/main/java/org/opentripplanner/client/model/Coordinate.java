package org.opentripplanner.client.model;

public record Coordinate(double lat, double lon) {

  @Override
  public String toString() {
    return "%s,%s".formatted(lat, lon);
  }
}
