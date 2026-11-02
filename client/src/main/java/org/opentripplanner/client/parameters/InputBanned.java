package org.opentripplanner.client.parameters;

import java.util.Set;

public class InputBanned {

  public InputBanned(
      Set<String> routes,
      Set<String> agencies,
      Set<String> trips,
      Set<String> stops,
      Set<String> stopsHard) {
    this.routes = routes;
    this.agencies = agencies;
    this.trips = trips;
    this.stops = stops;
    this.stopsHard = stopsHard;
  }

  Set<String> routes = Set.of();

  Set<String> agencies = Set.of();

  Set<String> trips = Set.of();

  Set<String> stops = Set.of();

  Set<String> stopsHard = Set.of();

  public static InputBannedBuilder builder() {
    return new InputBannedBuilder();
  }

  public org.opentripplanner.api.types.InputBanned toGenerated() {
    return org.opentripplanner.api.types.InputBanned.builder()
        .setRoutes(join(routes))
        .setAgencies(join(agencies))
        .setTrips(join(trips))
        .setStops(join(stops))
        .setStopsHard(join(stopsHard))
        .build();
  }

  private static String join(Set<String> ids) {
    return ids.isEmpty() ? null : String.join(",", ids);
  }

  @Override
  public String toString() {
    return toGenerated().toString();
  }
}
