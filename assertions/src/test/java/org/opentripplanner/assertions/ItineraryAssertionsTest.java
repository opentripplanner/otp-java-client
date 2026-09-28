package org.opentripplanner.assertions;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;
import org.opentripplanner.api.types.Route;
import org.opentripplanner.client.model.Agency;
import org.opentripplanner.client.model.Currency;
import org.opentripplanner.client.model.FareProductUse;
import org.opentripplanner.client.model.Itinerary;
import org.opentripplanner.client.model.Leg;
import org.opentripplanner.client.model.LegGeometry;
import org.opentripplanner.client.model.LegMode;
import org.opentripplanner.client.model.Money;
import org.opentripplanner.client.model.Place;
import org.opentripplanner.client.model.Stop;
import org.opentripplanner.client.model.Trip;
import org.opentripplanner.client.model.TripPlan;

class ItineraryAssertionsTest {

  private static final OffsetDateTime START = OffsetDateTime.parse("2026-01-01T08:00:00Z");
  private static final String GEOMETRY = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";

  @Test
  void singleLegMatchSuccess() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of())));

    assertDoesNotThrow(
        () ->
            new ItineraryAssertions()
                .hasLeg()
                .withMode("BUS")
                .withRouteShortName("10")
                .assertMatches(plan));
  }

  @Test
  void nonTransitLegsAreIgnored() {
    TripPlan plan =
        tripPlan(
            itinerary(
                walkLeg(Duration.ofMinutes(10)),
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of()),
                walkLeg(Duration.ofMinutes(10))));

    assertDoesNotThrow(
        () ->
            new ItineraryAssertions()
                .withStrictTransitMatching()
                .hasLeg()
                .withRouteShortName("10")
                .assertMatches(plan));
  }

  @Test
  void multiLegMatchSuccess() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of()),
                transitLeg("1", "Line 1", LegMode.TRAM, Duration.ofMinutes(15), List.of())));

    assertDoesNotThrow(
        () ->
            new ItineraryAssertions()
                .hasLeg()
                .withRouteShortName("10")
                .hasLeg()
                .withRouteShortName("1")
                .assertMatches(plan));
  }

  @Test
  void exactTransitLegsMustMatchInOrder() {
    TripPlan plan =
        tripPlan(
            itinerary(
                walkLeg(Duration.ofMinutes(5)),
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of()),
                transitLeg("1", "Line 1", LegMode.TRAM, Duration.ofMinutes(15), List.of()),
                walkLeg(Duration.ofMinutes(5))));

    assertDoesNotThrow(
        () ->
            new ItineraryAssertions()
                .withStrictTransitMatching()
                .hasLeg()
                .withRouteShortName("10")
                .hasLeg()
                .withRouteShortName("1")
                .assertMatches(plan));

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryAssertions()
                    .withStrictTransitMatching()
                    .hasLeg()
                    .withRouteShortName("1")
                    .hasLeg()
                    .withRouteShortName("10")
                    .assertMatches(plan));

    assertThat(error.getMessage()).contains("Transit leg at position 1 does not match");
    assertThat(error.isStrictTransitMatching()).isTrue();
  }

  @Test
  void nonStrictTransitLegsRemainUnordered() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of()),
                transitLeg("1", "Line 1", LegMode.TRAM, Duration.ofMinutes(15), List.of())));

    assertDoesNotThrow(
        () ->
            new ItineraryAssertions()
                .hasLeg()
                .withRouteShortName("1")
                .hasLeg()
                .withRouteShortName("10")
                .assertMatches(plan));
  }

  @Test
  void matchesBoardingAndAlightingStopsByCodeOrGtfsId() {
    Leg leg =
        transitLeg(
            "10",
            "Route 10",
            LegMode.BUS,
            Duration.ofMinutes(20),
            List.of(),
            place("Board", "feed:board", "BOARD"),
            place("Alight", "feed:alight", "ALIGHT"),
            false);
    TripPlan plan = tripPlan(itinerary(leg));

    assertDoesNotThrow(
        () ->
            new ItineraryAssertions()
                .hasLeg()
                .withBoardingStopCode("BOARD")
                .withBoardingStopGtfsId("feed:board")
                .withAlightingStopCode("ALIGHT")
                .withAlightingStopGtfsId("feed:alight")
                .assertMatches(plan));

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryAssertions()
                    .hasLeg()
                    .withBoardingStopCode("ALIGHT")
                    .withAlightingStopGtfsId("feed:board")
                    .assertMatches(plan));

    assertThat(error.getMessage()).contains("boarding stop code '[ALIGHT]'");
    assertThat(error.getMessage()).contains("alighting stop GTFS ID '[feed:board]'");
  }

  @Test
  void exactMatchingChecksInterliningAtTheExpectedTransition() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg(
                    "10",
                    "Route 10",
                    LegMode.BUS,
                    Duration.ofMinutes(10),
                    List.of(),
                    place("A"),
                    place("B"),
                    false),
                transitLeg(
                    "10",
                    "Route 10",
                    LegMode.BUS,
                    Duration.ofMinutes(10),
                    List.of(),
                    place("B"),
                    place("C"),
                    true)));

    assertDoesNotThrow(
        () ->
            new ItineraryAssertions()
                .withStrictTransitMatching()
                .hasLeg()
                .withRouteShortName("10")
                .hasLeg()
                .withRouteShortName("10")
                .interlinedWithPreviousLeg()
                .assertMatches(plan));

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryAssertions()
                    .withStrictTransitMatching()
                    .hasLeg()
                    .withRouteShortName("10")
                    .interlinedWithPreviousLeg()
                    .hasLeg()
                    .withRouteShortName("10")
                    .assertMatches(plan));

    assertThat(error.getMessage()).contains("Transit leg at position 1 does not match");
    assertThat(error.getMessage()).contains("interlined with previous leg");
  }

  @Test
  void strictMatchingRejectsExtraTransitLegs() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of()),
                transitLeg("1", "Line 1", LegMode.TRAM, Duration.ofMinutes(15), List.of())));

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryAssertions()
                    .withStrictTransitMatching()
                    .hasLeg()
                    .withRouteShortName("10")
                    .assertMatches(plan));

    assertThat(error.getMessage()).contains("additional transit legs");
    assertThat(error.getFailedResults()).hasSize(1);
    assertThat(error.getFailedResults().get(0).extraMatches()).hasSize(1);
  }

  @Test
  void partialMatchesAndErrorDetailsAreIncluded() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of())));

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryAssertions()
                    .hasLeg()
                    .withRouteShortName("10")
                    .withMode("TRAM")
                    .assertMatches(plan));

    assertThat(error.getFailedResults()).hasSize(1);
    ItineraryMatchResult result = error.getFailedResults().get(0);
    assertThat(result.errors()).isNotEmpty();
    assertThat(result.getPartialMatches()).hasSize(1);
    assertThat(result.getPartialMatches().get(0).getMatchingCriteria()).contains("route '[10]'");
    assertThat(result.getPartialMatches().get(0).getMissingCriteria()).contains("mode TRAM");
  }

  @Test
  void withFarePriceMatchesFareWithSpecifiedPriceRiderCategoryAndMedium() {
    List<FareProductUse> fares = List.of(fare(2.75f, "orca:regular", "orca:cash"));
    TripPlan plan =
        tripPlan(itinerary(transitLeg("E", "E Line", LegMode.BUS, Duration.ofMinutes(12), fares)));

    assertDoesNotThrow(
        () ->
            new ItineraryAssertions()
                .hasLeg()
                .withRouteShortName("E")
                .withFarePrice(2.75f, "orca:regular", "orca:cash")
                .assertMatches(plan));
  }

  @Test
  void withFarePriceReportsMismatchedFare() {
    List<FareProductUse> fares = List.of(fare(2.75f, "orca:regular", "orca:cash"));
    TripPlan plan =
        tripPlan(itinerary(transitLeg("E", "E Line", LegMode.BUS, Duration.ofMinutes(12), fares)));

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryAssertions()
                    .hasLeg()
                    .withRouteShortName("E")
                    .withFarePrice(3.00f, "orca:regular", "orca:cash")
                    .assertMatches(plan));

    String expectedFareCriterion = "fare $3.00 (rider category orca:regular, medium orca:cash)";
    assertThat(error.getExpectedLegs())
        .containsExactly(List.of("route '[E]'", expectedFareCriterion));
    assertThat(error.getFailedResults()).hasSize(1);
    assertThat(error.getFailedResults().get(0).getPartialMatches()).hasSize(1);
    LegMatchingState partialMatch = error.getFailedResults().get(0).getPartialMatches().get(0);
    assertThat(partialMatch.getMatchingCriteria()).isEqualTo("route '[E]'");
    assertThat(partialMatch.getMissingCriteria()).isEqualTo(expectedFareCriterion);
  }

  private static TripPlan tripPlan(Itinerary... itineraries) {
    return new TripPlan(List.of(itineraries), "", "");
  }

  private static Itinerary itinerary(Leg... legs) {
    return new Itinerary(List.of(legs), OptionalDouble.empty());
  }

  private static Leg walkLeg(Duration duration) {
    return new Leg(
        place("Walk From"),
        place("Walk To"),
        START,
        START.plus(duration),
        false,
        false,
        LegMode.WALK,
        duration,
        1000,
        Optional.empty(),
        null,
        null,
        List.of(),
        OptionalDouble.empty(),
        null,
        new LegGeometry(GEOMETRY),
        false,
        Optional.empty());
  }

  private static Leg transitLeg(
      String routeShortName,
      String routeLongName,
      LegMode mode,
      Duration duration,
      List<FareProductUse> fareProducts) {
    String idToken = routeShortName != null ? routeShortName : routeLongName;
    return transitLeg(
        routeShortName,
        routeLongName,
        mode,
        duration,
        fareProducts,
        place("From " + idToken),
        place("To " + idToken),
        false);
  }

  private static Leg transitLeg(
      String routeShortName,
      String routeLongName,
      LegMode mode,
      Duration duration,
      List<FareProductUse> fareProducts,
      Place from,
      Place to,
      boolean interlineWithPreviousLeg) {
    String idToken = routeShortName != null ? routeShortName : routeLongName;
    Route route =
        Route.builder()
            .setId("route-" + idToken)
            .setGtfsId("gtfs-" + idToken)
            .setShortName(routeShortName)
            .setLongName(routeLongName)
            .build();

    return new Leg(
        from,
        to,
        START,
        START.plus(duration),
        false,
        interlineWithPreviousLeg,
        mode,
        duration,
        1000,
        Optional.empty(),
        route,
        new Trip("trip-" + idToken, Optional.empty(), Optional.empty()),
        fareProducts,
        OptionalDouble.empty(),
        new Agency("agency", "Test Agency"),
        new LegGeometry(GEOMETRY),
        false,
        Optional.empty());
  }

  private static Place place(String name) {
    return new Place(
        name, 10.0f, 10.0f, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
  }

  private static Place place(String name, String stopGtfsId, String stopCode) {
    Stop stop =
        new Stop(
            name,
            stopGtfsId,
            Optional.ofNullable(stopCode),
            Optional.empty(),
            Optional.empty(),
            null);
    return new Place(
        name,
        10.0f,
        10.0f,
        Optional.of(stop),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());
  }

  private static FareProductUse fare(String riderCategoryId, String mediumId) {
    return new FareProductUse(
        "fare-" + riderCategoryId + "-" + mediumId,
        new FareProductUse.FareProduct(
            "product-" + riderCategoryId + "-" + mediumId,
            "Test fare",
            null,
            Optional.of(new FareProductUse.FareProduct.RiderCategory(riderCategoryId, "Rider")),
            Optional.of(new FareProductUse.FareProduct.FareMedium(mediumId, "Medium"))));
  }

  private static FareProductUse fare(Float amount, String riderCategoryId, String mediumId) {
    return new FareProductUse(
        "fare-" + riderCategoryId + "-" + mediumId,
        new FareProductUse.FareProduct(
            "product-" + riderCategoryId + "-" + mediumId,
            "Test fare",
            new Money(BigDecimal.valueOf(amount), new Currency(2, "USD")),
            Optional.of(new FareProductUse.FareProduct.RiderCategory(riderCategoryId, "Rider")),
            Optional.of(new FareProductUse.FareProduct.FareMedium(mediumId, "Medium"))));
  }
}
