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

  @Override
  public String toString() {
    String routesString =
        routes.isEmpty() ? "" : String.format("routes: \"%s\"", String.join(",", routes));
    String agenciesString =
        agencies.isEmpty() ? "" : String.format("agencies: \"%s\"", String.join(",", agencies));
    String tripsString =
        trips.isEmpty() ? "" : String.format("trips: \"%s\"", String.join(",", trips));
    String stopsString =
        stops.isEmpty() ? "" : String.format("stops: \"%s\"", String.join(",", stops));
    String stopsHardString =
        stopsHard.isEmpty() ? "" : String.format("stopsHard: \"%s\"", String.join(",", stopsHard));

    return String.format(
        "{%s %s %s %s %s}",
        routesString, agenciesString, tripsString, stopsString, stopsHardString);
  }
}
