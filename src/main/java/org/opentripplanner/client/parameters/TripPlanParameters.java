package org.opentripplanner.client.parameters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.annotation.Nullable;
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
  @Nullable
  private final Duration searchWindow;
  private final float walkReluctance;
  private final boolean wheelchair;

  public TripPlanParameters(
      PlaceParameter fromPlace,
      PlaceParameter toPlace,
      LocalDateTime time,
      int numItineraries,
      Set<RequestMode> modes,
      SearchDirection searchDirection,
      Duration searchWindow,
      float walkReluctance,
      boolean wheelchair
  ) {
    CollectionUtils.assertHasValue(modes);

    this.fromPlace = Objects.requireNonNull(fromPlace);
    this.toPlace = Objects.requireNonNull(toPlace);
    this.time = Objects.requireNonNull(time);
    this.numItineraries = numItineraries;
    this.modes = Objects.requireNonNull(modes);
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
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (obj == null || obj.getClass() != this.getClass()) return false;
    var that = (TripPlanParameters) obj;
    return Objects.equals(this.fromPlace, that.fromPlace)
        && Objects.equals(this.toPlace, that.toPlace)
        && Objects.equals(this.time, that.time)
        && this.numItineraries == that.numItineraries
        && Objects.equals(this.modes, that.modes)
        && Objects.equals(this.searchDirection, that.searchDirection)
        && Objects.equals(this.searchWindow, that.searchWindow)
        && Float.floatToIntBits(this.walkReluctance) == Float.floatToIntBits(that.walkReluctance)
        && this.wheelchair == that.wheelchair;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        fromPlace,
        toPlace,
        time,
        numItineraries,
        modes,
        searchDirection,
        searchWindow,
        walkReluctance,
        wheelchair);
  }

  @Override
  public String toString() {
    return "TripPlanParameters["
        + "fromPlace="
        + fromPlace
        + ", "
        + "toPlace="
        + toPlace
        + ", "
        + "time="
        + time
        + ", "
        + "numItineraries="
        + numItineraries
        + ", "
        + "modes="
        + modes
        + ", "
        + "searchDirection="
        + searchDirection
        + ", "
        + "searchWindow="
        + searchWindow
        + ", "
        + "walkReluctance="
        + walkReluctance
        + ", "
        + "wheelchair="
        + wheelchair
        + ']';
  }
}
