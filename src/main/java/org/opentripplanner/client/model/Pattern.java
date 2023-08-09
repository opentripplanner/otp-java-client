package org.opentripplanner.client.model;

import java.util.List;

public record Pattern(String name, List<VehiclePosition> vehiclePositions) {
  public record VehiclePosition(String vehicleId) {}
}
