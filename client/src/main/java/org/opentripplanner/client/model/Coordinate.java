package org.opentripplanner.client.model;

public record Coordinate(double lat, double lon, String name) implements PlaceParameter {
  public Coordinate(double lat, double lon) {
    this(lat, lon, String.format("%s,%s", lat, lon));
  }

  @Override
  public String toString() {
    return "%s,%s".formatted(lat, lon);
  }

  @Override
  public String toPlaceString() {
    return String.format("%s::%s,%s", name, lat, lon);
  }
}
