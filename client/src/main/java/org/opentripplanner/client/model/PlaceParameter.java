package org.opentripplanner.client.model;

public sealed interface PlaceParameter permits Coordinate, StopId {
  String toPlaceString();
}
