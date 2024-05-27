package org.opentripplanner.client.parameters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentripplanner.client.model.PlaceParameter;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.parameters.TripPlanParameters.OptimizeType;
import org.opentripplanner.client.parameters.TripPlanParameters.SearchDirection;

public class TripPlanParametersBuilder {
  private PlaceParameter fromPlace;
  private PlaceParameter toPlace;
  private LocalDateTime time;
  private Set<RequestMode> modes;
  private SearchDirection searchDirection = SearchDirection.DEPART_AT;
  private Duration searchWindow;
  private float walkReluctance = 1.4f;
  private float carReluctance = 3.0f;
  private float bikeReluctance = 2.0f;
  private float bikeWalkingReluctance = 5.0f;
  private OptimizeType optimize = OptimizeType.QUICK;
  private InputTriangle triangle;
  private int numItineraries = 5;
  private boolean wheelchair = false;
  private InputBanned banned;

  public TripPlanParametersBuilder withFrom(PlaceParameter from) {
    this.fromPlace = from;
    return this;
  }

  public TripPlanParametersBuilder withTo(PlaceParameter coordinate) {
    this.toPlace = coordinate;
    return this;
  }

  public TripPlanParametersBuilder withTime(LocalDateTime time) {
    this.time = time;
    return this;
  }

  public TripPlanParametersBuilder withModes(Set<RequestMode> modes) {
    this.modes = modes;
    return this;
  }

  public TripPlanParametersBuilder withModes(RequestMode... modes) {
    this.modes = Arrays.stream(modes).collect(Collectors.toUnmodifiableSet());
    return this;
  }

  public TripPlanParametersBuilder withSearchDirection(SearchDirection searchDirection) {
    this.searchDirection = searchDirection;
    return this;
  }

  public TripPlanParametersBuilder withSearchWindow(Duration searchWindow) {
    this.searchWindow = searchWindow;
    return this;
  }

  public TripPlanParametersBuilder withWalkReluctance(float wr) {
    this.walkReluctance = wr;
    return this;
  }

  public TripPlanParametersBuilder withCarReluctance(float cr) {
    this.carReluctance = cr;
    return this;
  }

  public TripPlanParametersBuilder withBikeReluctance(float br) {
    this.bikeReluctance = br;
    return this;
  }

  public TripPlanParametersBuilder withBikeWalkingReluctance(float bwr) {
    this.bikeWalkingReluctance = bwr;
    return this;
  }

  public TripPlanParametersBuilder withNumberOfItineraries(int ni) {
    this.numItineraries = ni;
    return this;
  }

  public TripPlanParametersBuilder withWheelchair(boolean wheelchair) {
    this.wheelchair = wheelchair;
    return this;
  }

  public TripPlanParametersBuilder withBanned(InputBanned banned) {
    this.banned = banned;
    return this;
  }

  public TripPlanParametersBuilder withOptimize(OptimizeType optimize) {
    this.optimize = optimize;
    return this;
  }

  public TripPlanParametersBuilder withTriangle(InputTriangle triangle) {
    this.triangle = triangle;
    return this;
  }

  public TripPlanParametersBuilder copy() {
    return TripPlanParameters.builder()
        .withFrom(fromPlace)
        .withTo(toPlace)
        .withTime(time)
        .withModes(modes)
        .withSearchDirection(searchDirection)
        .withSearchWindow(searchWindow)
        .withWalkReluctance(walkReluctance)
        .withCarReluctance(carReluctance)
        .withBikeReluctance(bikeReluctance)
        .withBikeWalkingReluctance(bikeWalkingReluctance)
        .withNumberOfItineraries(numItineraries)
        .withWheelchair(wheelchair)
        .withBanned(banned)
        .withOptimize(optimize)
        .withTriangle(triangle);
  }

  public TripPlanParameters build() {
    return new TripPlanParameters(
        fromPlace,
        toPlace,
        time,
        numItineraries,
        modes,
        searchDirection,
        searchWindow,
        walkReluctance,
        carReluctance,
        bikeReluctance,
        bikeWalkingReluctance,
        wheelchair,
        banned,
        optimize,
        triangle);
  }
}
