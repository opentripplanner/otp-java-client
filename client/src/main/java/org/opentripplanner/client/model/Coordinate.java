package org.opentripplanner.client.model;

public record Coordinate(double lat, double lon) implements PlaceParameter {

  @Override
  public String toString() {
    return "%s,%s".formatted(lat, lon);
  }

  @Override
  public String toPlaceString() {
    return String.format("%s,%s::%s,%s", lat, lon, lat, lon);
  }
}
