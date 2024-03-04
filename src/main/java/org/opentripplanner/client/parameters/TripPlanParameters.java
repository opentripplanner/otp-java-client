package org.opentripplanner.client.parameters;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import org.opentripplanner.client.model.Coordinate;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.validation.CollectionUtils;

public record TripPlanParameters(
    Coordinate from,
    String fromPlace,
    Coordinate to,
    String toPlace,
    LocalDateTime time,
    int numItineraries,
    Set<RequestMode> modes,
    SearchDirection searchDirection,
    float walkReluctance,
    boolean wheelchair) {

  public TripPlanParameters {
    if (from == null && fromPlace == null || from != null && fromPlace != null) {
      throw new IllegalArgumentException("One of from and fromPlace must be provided");
    }
    if (to == null && toPlace == null || to != null && toPlace != null) {
      throw new IllegalArgumentException("One of to or toPlace must be provided");
    }
    Objects.requireNonNull(time);
    Objects.requireNonNull(modes);
    CollectionUtils.assertHasValue(modes);
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
