package org.opentripplanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opentripplanner.StationParameters.OSLO_EAST;
import static org.opentripplanner.StationParameters.OSLO_LUFTHAVN_ID;
import static org.opentripplanner.StationParameters.OSLO_LUFTHAVN_QUAY;
import static org.opentripplanner.StationParameters.OSLO_S_ID;
import static org.opentripplanner.StationParameters.OSLO_WEST;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentripplanner.client.OtpApiClient;
import org.opentripplanner.client.model.Coordinate;
import org.opentripplanner.client.model.FareProductUse;
import org.opentripplanner.client.model.Leg;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.model.TripPlan;
import org.opentripplanner.client.parameters.InputBanned;
import org.opentripplanner.client.parameters.InputTriangle;
import org.opentripplanner.client.parameters.TripPlanParameters;
import org.opentripplanner.client.parameters.TripPlanParameters.SearchDirection;
import org.opentripplanner.client.parameters.TripPlanParametersBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Execution(ExecutionMode.CONCURRENT)
public class IntegrationTest {
  public static final Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);

  public static OtpApiClient client =
      new OtpApiClient(ZoneId.of("Europe/Oslo"), "https://otp2debug.entur.org");

  @Test
  public void plan() throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_WEST)
                .withTo(OSLO_EAST)
                .withTime(LocalDateTime.now())
                .withModes(RequestMode.TRANSIT)
                .withNumberOfItineraries(3)
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());

    var leg = result.itineraries().get(0).legs().get(0);

    var transitLeg =
        result.transitItineraries().stream()
            .filter(i -> i.legs().stream().anyMatch(l -> l.intermediatePlaces().isPresent()))
            .findFirst()
            .get()
            .transitLegs()
            .get(0);
    assertFalse(transitLeg.from().stop().isEmpty());
    assertNotNull(transitLeg.from().coordinate());
    assertNotNull(transitLeg.from().point());
    assertFalse(transitLeg.to().stop().isEmpty());
    assertNotNull(transitLeg.to().coordinate());
    assertNotNull(transitLeg.to().point());
    assertNotNull(transitLeg.from().stop().get().id());
    assertTrue(transitLeg.trip().headsign().isPresent());
    assertNotNull(transitLeg.agency());
    assertNotNull(transitLeg.intermediatePlaces().get().get(0).name());
    assertNotNull(transitLeg.intermediatePlaces().get().get(0).departureTime());
    assertNotNull(transitLeg.intermediatePlaces().get().get(0).arrivalTime());
    assertNotNull(transitLeg.geometry().toGoogleEncoding());
    assertNotNull(transitLeg.geometry().toLinestring());

    assertEquals(List.of(), leg.fareProducts());
  }

  @Test
  public void planPlaceToPlace() throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_LUFTHAVN_ID)
                .withTo(OSLO_S_ID)
                .withTime(LocalDateTime.now())
                .withModes(RequestMode.TRANSIT)
                .withNumberOfItineraries(3)
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());

    var leg = result.itineraries().get(0).legs().get(0);

    var transitLeg = result.transitItineraries().get(0).transitLegs().get(0);
    assertFalse(transitLeg.from().stop().isEmpty());
    assertFalse(transitLeg.to().stop().isEmpty());
    assertNotNull(transitLeg.from().stop().get().id());

    assertEquals(List.of(), leg.fareProducts());
  }

  @Test
  public void planPlaceToPlaceWithSearchWindow() throws IOException {
    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_LUFTHAVN_ID)
                .withTo(OSLO_S_ID)
                .withTime(LocalDateTime.now())
                .withModes(RequestMode.TRANSIT)
                .withNumberOfItineraries(1)
                .withSearchWindow(Duration.ofDays(1))
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());
    assertEquals(1, result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());

    var leg = result.itineraries().get(0).legs().get(0);

    var transitLeg = result.transitItineraries().get(0).transitLegs().get(0);
    assertFalse(transitLeg.from().stop().isEmpty());
    assertFalse(transitLeg.to().stop().isEmpty());
    assertNotNull(transitLeg.from().stop().get().id());

    assertEquals(List.of(), leg.fareProducts());
  }

  @Test
  public void planPlaceToPlaceWithBanned() throws IOException {

    final String BANNED_AGENCY = "RB:FLT:Authority:FLT";
    final String BANNED_ROUTE = "RB:NSB:Line:L12";
    final String BANNED_TRIP = "RB:NSB:ServiceJourney:324_173570-R";

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_LUFTHAVN_ID)
                .withTo(OSLO_S_ID)
                .withTime(LocalDateTime.now())
                .withModes(RequestMode.TRANSIT)
                .withBanned(
                    InputBanned.builder()
                        .withAgencies(Set.of(BANNED_AGENCY))
                        .withRoutes(Set.of(BANNED_ROUTE))
                        .withTrips(Set.of(BANNED_TRIP))
                        .build())
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertFalse(checkTransitLegCondition(result, leg -> leg.agency().id().equals(BANNED_AGENCY)));

    assertFalse(checkTransitLegCondition(result, leg -> leg.route().id().equals(BANNED_ROUTE)));

    assertFalse(checkTransitLegCondition(result, leg -> leg.trip().id().equals(BANNED_TRIP)));
  }

  private static boolean checkTransitLegCondition(TripPlan result, Predicate<Leg> condition) {
    return result.itineraries().stream()
        .anyMatch(itinerary -> itinerary.transitLegs().stream().anyMatch(condition));
  }

  @Test
  public void planPlaceToPlaceWithTriangle() throws IOException {
    InputTriangle safeWayTriangle =
        InputTriangle.builder()
            .withSafetyFactor(1.0f)
            .withSlopeFactor(0.0f)
            .withTimeFactor(0.0f)
            .build();

    InputTriangle fastWayTriangle =
        InputTriangle.builder()
            .withSafetyFactor(0.0f)
            .withSlopeFactor(0.0f)
            .withTimeFactor(1.0f)
            .build();

    TripPlanParametersBuilder builder =
        TripPlanParameters.builder()
            .withFrom(OSLO_WEST)
            .withTo(OSLO_EAST)
            .withTime(LocalDateTime.now())
            .withModes(RequestMode.BICYCLE)
            .withOptimize(TripPlanParameters.OptimizeType.TRIANGLE);

    builder.withTriangle(safeWayTriangle);
    var safeResult = client.plan(builder.build());

    builder.withTriangle(fastWayTriangle);
    var fastResult = client.plan(builder.build());

    LOG.info("Received {} safe itineraries", safeResult.itineraries().size());
    LOG.info("Received {} fast itineraries", fastResult.itineraries().size());

    Leg saveLeg = safeResult.itineraries().get(0).legs().get(0);
    Leg fastLeg = fastResult.itineraries().get(0).legs().get(0);

    assertNotNull(saveLeg.startTime());
    assertNotNull(fastLeg.startTime());
    assertTrue(fastLeg.duration().getSeconds() < saveLeg.duration().getSeconds());
  }

  @Test
  public void planWithReluctance() throws IOException {
    TripPlanParametersBuilder builder =
        TripPlanParameters.builder()
            .withFrom(OSLO_WEST)
            .withTo(OSLO_EAST)
            .withTime(LocalDateTime.now())
            .withModes(Set.of(RequestMode.WALK, RequestMode.TRANSIT));

    assertEquals(Optional.empty(), builder.build().walkReluctance());
    assertEquals(Optional.empty(), builder.build().bikeReluctance());
    assertEquals(Optional.empty(), builder.build().carReluctance());
    assertEquals(Optional.empty(), builder.build().bikeWalkingReluctance());

    // Plan with high walk reluctance - should prefer transit
    builder.withWalkReluctance(5.0f);
    builder.withBikeReluctance(4.0f);
    builder.withCarReluctance(3.0f);
    builder.withBikeWalkingReluctance(2.0f);
    assertEquals(Optional.of(5.0f), builder.build().walkReluctance());
    assertEquals(Optional.of(4.0f), builder.build().bikeReluctance());
    assertEquals(Optional.of(3.0f), builder.build().carReluctance());
    assertEquals(Optional.of(2.0f), builder.build().bikeWalkingReluctance());
  }

  @Test
  public void arriveByPlan() throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_WEST)
                .withTo(OSLO_EAST)
                .withTime(LocalDateTime.now())
                .withModes(RequestMode.TRANSIT)
                .withSearchDirection(SearchDirection.ARRIVE_BY)
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());
  }

  public static List<Set<RequestMode>> cases() {
    return List.of(
        Set.of(RequestMode.BICYCLE),
        Set.of(RequestMode.BICYCLE_PARK, RequestMode.TRANSIT),
        Set.of(RequestMode.CAR),
        Set.of(RequestMode.CAR_PARK, RequestMode.TRANSIT));
  }

  @ParameterizedTest
  @MethodSource("cases")
  public void modes(Set<RequestMode> modes) throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_WEST)
                .withTo(OSLO_EAST)
                .withTime(LocalDateTime.now())
                .withModes(modes)
                .build());

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());
  }

  @Test
  public void rentalStations() throws IOException {

    var result = client.vehicleRentalStations();

    LOG.info("Received {} rental stations", result.size());

    assertFalse(result.isEmpty());
  }

  @Test
  public void routes() throws IOException {
    var routes = client.routes();
    LOG.info("Received {} routes", routes.size());

    assertFalse(routes.isEmpty());
    routes.forEach(
        r -> {
          assertFalse(r.name().isEmpty(), "Route %s has no name.".formatted(r));
          assertFalse(r.agency().name().isEmpty());
        });
  }

  @Test
  public void patterns() throws IOException {

    var result = client.patterns();

    LOG.info("Received {} patterns", result.size());

    assertFalse(result.isEmpty());

    result.forEach(
        pattern -> {
          assertNotNull(pattern.name());
          assertNotNull(pattern.vehiclePositions());
        });
  }

  @Test
  public void agencies() throws IOException {

    var result = client.agencies();

    LOG.info("Received {} agencies", result.size());

    assertFalse(result.isEmpty());

    result.forEach(
        agency -> {
          assertNotNull(agency.name());
          assertNotNull(agency.id());
        });
  }

  @Test
  public void stop() throws IOException {

    var result = client.stop(OSLO_LUFTHAVN_QUAY);

    LOG.info("Received stop");

    assertNotNull(result);
    assertNotNull(result.name());
    assertNotNull(result.id());
  }

  @Test
  public void stops() throws IOException {
    var result = client.stopSearch("Oslo");

    LOG.info("Received stops");

    assertNotNull(result);
    assertFalse(result.isEmpty());

    var stop = result.get(0);

    assertNotNull(stop.id());
  }

  @Test
  public void alerts() throws IOException {
    var result = client.alerts();

    LOG.info("Received {} alerts", result.size());

    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Disabled
  @Test
  public void seattleFares() throws IOException {

    var southSeattle = new Coordinate(47.5634, -122.3155);
    var northSeattle = new Coordinate(47.6225, -122.3312);
    var client = new OtpApiClient(ZoneId.of("America/Los_Angeles"), "http://localhost:8080");

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(southSeattle)
                .withTo(northSeattle)
                .withTime(LocalDateTime.now())
                .withModes(Set.of(RequestMode.TRANSIT))
                .build());

    var itin = result.itineraries().get(1);

    var transitLeg = itin.legs().get(1);

    var product =
        transitLeg.fareProducts().stream()
            .map(FareProductUse::product)
            .filter(fp -> fp.id().equals("orca:farePayment"))
            .findFirst()
            .get();

    assertNotNull(product.price());
    assertNotNull(product.price().currency());
    assertNotNull(product.price().amount());
    assertNotNull(product.medium().get().id());
    assertNotNull(product.medium().get().name());
    assertNotNull(product.riderCategory().get().id());
    assertNotNull(product.riderCategory().get().name());
  }
}
