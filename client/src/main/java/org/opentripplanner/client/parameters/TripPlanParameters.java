package org.opentripplanner.client.parameters;

import com.google.common.base.MoreObjects;
import jakarta.annotation.Nullable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.opentripplanner.api.types.OptimizeType;
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
  @Nullable private final Double walkReluctance;
  @Nullable private final Double carReluctance;
  @Nullable private final Double bikeReluctance;
  @Nullable private final Double bikeWalkingReluctance;
  @Nullable private final Double walkSpeed;
  private final boolean wheelchair;
  @Nullable private final InputBanned banned;
  private final OptimizeType optimize;
  @Nullable private final InputTriangle triangle;
  @Nullable private final String pageCursor;

  public TripPlanParameters(
      PlaceParameter fromPlace,
      PlaceParameter toPlace,
      LocalDateTime time,
      int numItineraries,
      Set<RequestMode> modes,
      SearchDirection searchDirection,
      @Nullable Duration searchWindow,
      @Nullable Double walkReluctance,
      @Nullable Double carReluctance,
      @Nullable Double bikeReluctance,
      @Nullable Double bikeWalkingReluctance,
      @Nullable Double walkSpeed,
      boolean wheelchair,
      @Nullable InputBanned banned,
      OptimizeType optimize,
      @Nullable InputTriangle triangle,
      @Nullable String pageCursor) {
    this.fromPlace = Objects.requireNonNull(fromPlace);
    this.toPlace = Objects.requireNonNull(toPlace);
    this.time = Objects.requireNonNull(time);
    this.numItineraries = numItineraries;
    this.modes = Set.copyOf(CollectionUtils.assertHasValue(modes));
    this.searchDirection = Objects.requireNonNull(searchDirection);
    this.searchWindow = searchWindow;
    this.walkReluctance = walkReluctance;
    this.carReluctance = carReluctance;
    this.bikeReluctance = bikeReluctance;
    this.bikeWalkingReluctance = bikeWalkingReluctance;
    this.walkSpeed = walkSpeed;
    this.wheelchair = wheelchair;
    this.banned = banned;
    this.optimize = optimize;
    this.triangle = triangle;
    this.pageCursor = pageCursor;
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

  public Optional<Double> walkReluctance() {
    return Optional.ofNullable(walkReluctance);
  }

  public Optional<Double> carReluctance() {
    return Optional.ofNullable(carReluctance);
  }

  public Optional<Double> bikeReluctance() {
    return Optional.ofNullable(bikeReluctance);
  }

  public Optional<Double> bikeWalkingReluctance() {
    return Optional.ofNullable(bikeWalkingReluctance);
  }

  public Optional<Double> walkSpeed() {
    return Optional.ofNullable(walkSpeed);
  }

  public boolean wheelchair() {
    return wheelchair;
  }

  public Optional<InputBanned> banned() {
    return Optional.ofNullable(banned);
  }

  public OptimizeType optimize() {
    return optimize;
  }

  public Optional<InputTriangle> triangle() {
    return Optional.ofNullable(triangle);
  }

  public Optional<String> pageCursor() {
    return Optional.ofNullable(pageCursor);
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
        .add("carReluctance", carReluctance)
        .add("bikeReluctance", bikeReluctance)
        .add("bikeWalkReluctance", bikeWalkingReluctance)
        .add("wheelchair", wheelchair)
        .add("banned", banned)
        .add("optimize", optimize)
        .add("triangle", triangle)
        .toString();
  }
}
