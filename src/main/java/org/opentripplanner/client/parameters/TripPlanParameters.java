package org.opentripplanner.client.parameters;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import org.opentripplanner.client.model.Coordinate;
import org.opentripplanner.client.model.RequestMode;

public record TripPlanParameters(
    Coordinate from,
    Coordinate to,
    LocalDateTime time,
    Set<RequestMode> modes,
    SearchDirection searchDirection) {

  public TripPlanParameters {
    Objects.requireNonNull(from);
    Objects.requireNonNull(to);
    Objects.requireNonNull(time);
    Objects.requireNonNull(modes);
    Objects.requireNonNull(searchDirection);
  }

  public enum SearchDirection {
    DEPART_AT,
    ARRIVE_BY;

    public boolean isArriveBy() {
      return this == ARRIVE_BY;
    }
  }

  public static TripPlanParametersBuilder builder() {
    return new TripPlanParametersBuilder();
  }
}
