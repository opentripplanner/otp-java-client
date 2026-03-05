package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import org.opentripplanner.api.types.Route;

public record Leg(
    Place from,
    Place to,
    OffsetDateTime startTime,
    OffsetDateTime endTime,
    Boolean realTime,
    boolean interlineWithPreviousLeg,
    LegMode mode,
    Duration duration,
    double distance,
    Optional<String> headsign,
    Route route,
    Trip trip,
    List<FareProductUse> fareProducts,
    OptionalDouble accessibilityScore,
    Agency agency,
    @JsonProperty("legGeometry") LegGeometry geometry,
    @JsonProperty("rentedBike") boolean rentedVehicle,
    Optional<List<IntermediatePlace>> intermediatePlaces) {

  /** Is this leg using public transport? */
  public boolean isTransit() {
    return mode.isTransit();
  }

  public String formatLegDescription() {
    if (this.isTransit()) {
      String routeName = routeDisplayName();
      String interlinedText = this.interlineWithPreviousLeg() ? " (interlined)" : "";
      return "TRANSIT - Route: %s%s, From: %s, To: %s%n"
          .formatted(routeName, interlinedText, this.from().name(), this.to().name());
    }

    return "%s - From: %s, To: %s, Distance: %.0fm%n"
        .formatted(
            this.mode().toString().toUpperCase(),
            this.from().name(),
            this.to().name(),
            this.distance());
  }

  public String routeDisplayName() {
    String fallback = this.mode().toString();
    if (this.route() == null) {
      return fallback;
    }
    String shortName = this.route().getShortName();
    if (shortName != null) {
      return shortName;
    }
    String longName = this.route().getLongName();
    if (longName != null) {
      return longName;
    }
    return fallback;
  }
}
