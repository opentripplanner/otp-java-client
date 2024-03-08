package org.opentripplanner;

import org.opentripplanner.client.model.Coordinate;
import org.opentripplanner.client.model.StopId;

public interface StationParameters {
  Coordinate OSLO_EAST = new Coordinate(59.9132, 10.7692);
  Coordinate OSLO_WEST = new Coordinate(59.9203, 10.6823);
  StopId OSLO_LUFTHAVN_ID = new StopId("RB:NSR:StopPlace:5357");
  StopId OSLO_S_ID = new StopId("RB:NSR:StopPlace:337");
  String OSLO_LUFTHAVN_QUAY = "RB:NSR:Quay:9786";
}
