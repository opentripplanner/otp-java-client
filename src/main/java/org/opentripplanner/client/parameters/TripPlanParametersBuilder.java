package org.opentripplanner.client.parameters;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentripplanner.client.model.Coordinate;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.parameters.TripPlanParameters.SearchDirection;

public class TripPlanParametersBuilder {
  private Coordinate from;
  private String fromPlace;
  private Coordinate to;
  private String toPlace;
  private LocalDateTime time;
  private Set<RequestMode> modes;
  private SearchDirection searchDirection = SearchDirection.DEPART_AT;
  private float walkReluctance = 1.4f;
  private int numItineraries = 5;
  private boolean wheelchair = false;

  public TripPlanParametersBuilder withFrom(Coordinate from) {
    this.from = from;
    return this;
  }

  public TripPlanParametersBuilder withFromPlace(String fromPlace) {
    this.fromPlace = fromPlace;
    return this;
  }

  public TripPlanParametersBuilder withTo(Coordinate coordinate) {
    this.to = coordinate;
    return this;
  }

  public TripPlanParametersBuilder withToPlace(String toPlace) {
    this.toPlace = toPlace;
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

  public TripPlanParametersBuilder withWalkReluctance(float wr) {
    this.walkReluctance = wr;
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

  public TripPlanParametersBuilder copy() {
    return TripPlanParameters.builder()
        .withFrom(from)
        .withFromPlace(fromPlace)
        .withTo(to)
        .withToPlace(toPlace)
        .withTime(time)
        .withModes(modes)
        .withSearchDirection(searchDirection)
        .withWalkReluctance(walkReluctance)
        .withNumberOfItineraries(numItineraries)
        .withWheelchair(wheelchair);
  }

  public TripPlanParameters build() {
    return new TripPlanParameters(
        from,
        fromPlace,
        to,
        toPlace,
        time,
        numItineraries,
        modes,
        searchDirection,
        walkReluctance,
        wheelchair);
  }
}
