package org.opentripplanner.client.parameters;

import com.google.common.base.MoreObjects;
import jakarta.annotation.Nullable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.opentripplanner.client.model.PlaceParameter;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.validation.CollectionUtils;

public final class TripPlanParameters {
  private final PlaceParameter fromPlace;
  private final PlaceParameter toPlace;
  private final LocalDateTime time;
  private final int numItineraries;
  private final Set<RequestMode> modes;
  private final SearchDirection searchDirection;
  @Nullable private final Duration searchWindow;
  private final float walkReluctance;
  private final boolean wheelchair;

  public TripPlanParameters(
      PlaceParameter fromPlace,
      PlaceParameter toPlace,
      LocalDateTime time,
      int numItineraries,
      Set<RequestMode> modes,
      SearchDirection searchDirection,
      @Nullable Duration searchWindow,
      float walkReluctance,
      boolean wheelchair) {

    this.fromPlace = Objects.requireNonNull(fromPlace);
    this.toPlace = Objects.requireNonNull(toPlace);
    this.time = Objects.requireNonNull(time);
    this.numItineraries = numItineraries;
    this.modes = Set.copyOf(CollectionUtils.assertHasValue(modes));
    this.searchDirection = Objects.requireNonNull(searchDirection);
    this.searchWindow = searchWindow;
    this.walkReluctance = walkReluctance;
    this.wheelchair = wheelchair;
  }

  public Optional<Duration> searchWindow() {
    return Optional.ofNullable(searchWindow);
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

  public PlaceParameter fromPlace() {
    return fromPlace;
  }

  public PlaceParameter toPlace() {
    return toPlace;
  }

  public LocalDateTime time() {
    return time;
  }

  public int numItineraries() {
    return numItineraries;
  }

  public Set<RequestMode> modes() {
    return modes;
  }

  public SearchDirection searchDirection() {
    return searchDirection;
  }

  public float walkReluctance() {
    return walkReluctance;
  }

  public boolean wheelchair() {
    return wheelchair;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("fromPlace", fromPlace)
        .add("toPlace", toPlace)
        .add("time", time)
        .add("numItineraries", numItineraries)
        .add("modes", modes)
        .add("searchDirection", searchDirection)
        .add("searchWindow", searchWindow)
        .add("walkReluctance", walkReluctance)
        .add("wheelchair", wheelchair)
        .toString();
  }
}
