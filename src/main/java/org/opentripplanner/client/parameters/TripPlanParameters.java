package org.opentripplanner.client.parameters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.opentripplanner.client.model.PlaceParameter;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.validation.CollectionUtils;

public record TripPlanParameters(
    PlaceParameter fromPlace,
    PlaceParameter toPlace,
    LocalDateTime time,
    int numItineraries,
    Set<RequestMode> modes,
    SearchDirection searchDirection,
    Duration searchWindow,
    float walkReluctance,
    boolean wheelchair) {

  public TripPlanParameters {
    Objects.requireNonNull(fromPlace);
    Objects.requireNonNull(toPlace);
    Objects.requireNonNull(time);
    Objects.requireNonNull(modes);
    CollectionUtils.assertHasValue(modes);
    Objects.requireNonNull(searchDirection);
  }

  public Optional<Duration> getSearchWindow() {
    return Optional.ofNullable(searchWindow());
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
