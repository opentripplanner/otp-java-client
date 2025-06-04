package org.opentripplanner.client.parameters;

import java.util.Set;

public class InputBannedBuilder {
  private Set<String> routes = Set.of();
  private Set<String> agencies = Set.of();
  private Set<String> trips = Set.of();
  private Set<String> stops = Set.of();
  private Set<String> stopsHard = Set.of();

  public InputBannedBuilder withRoutes(Set<String> routes) {
    this.routes = routes;
    return this;
  }

  public InputBannedBuilder withAgencies(Set<String> agencies) {
    this.agencies = agencies;
    return this;
  }

  public InputBannedBuilder withTrips(Set<String> trips) {
    this.trips = trips;
    return this;
  }

  public InputBannedBuilder withStops(Set<String> stops) {
    this.stops = stops;
    return this;
  }

  public InputBannedBuilder withStopsHard(Set<String> stopsHard) {
    this.stopsHard = stopsHard;
    return this;
  }

  public InputBanned copy() {
    return new InputBannedBuilder()
        .withRoutes(this.routes)
        .withAgencies(this.agencies)
        .withTrips(this.trips)
        .withStops(this.stops)
        .withStopsHard(this.stopsHard)
        .build();
  }

  public InputBanned build() {
    return new InputBanned(routes, agencies, trips, stops, stopsHard);
  }
}
