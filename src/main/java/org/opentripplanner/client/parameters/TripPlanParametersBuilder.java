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
  private Coordinate coordinate;
  private LocalDateTime time;
  private Set<RequestMode> modes;
  private SearchDirection searchDirection = SearchDirection.DEPART_AT;
  private float walkReluctance = 1.4f;

  public TripPlanParametersBuilder withFrom(Coordinate from) {
    this.from = from;
    return this;
  }

  public TripPlanParametersBuilder withTo(Coordinate coordinate) {
    this.coordinate = coordinate;
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

  public TripPlanParameters build() {
    return new TripPlanParameters(from, coordinate, time, modes, searchDirection, walkReluctance);
  }
}
