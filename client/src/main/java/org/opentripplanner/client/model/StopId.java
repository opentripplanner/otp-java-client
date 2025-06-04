package org.opentripplanner.client.model;

public record StopId(String id) implements PlaceParameter {
  @Override
  public String toPlaceString() {
    return id;
  }
}
