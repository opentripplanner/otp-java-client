package org.opentripplanner.client.model;

import java.util.EnumSet;
import java.util.Set;

public enum LegMode {
  WALK,
  RAIL,
  BUS,
  SUBWAY,
  FERRY,
  TRAM,
  MONORAIL,
  CARPOOL,
  BICYCLE,
  SCOOTER,
  CAR,
  TAXI,
  CABLE_CAR,
  AIRPLANE,
  FUNICULAR,
  FLEX,
  GONDOLA,
  TROLLEYBUS;

  private static final Set<LegMode> NON_TRANSIT_MODES = EnumSet.of(CAR, SCOOTER, BICYCLE, WALK);

  public boolean isTransit() {
    return !NON_TRANSIT_MODES.contains(this);
  }
}
