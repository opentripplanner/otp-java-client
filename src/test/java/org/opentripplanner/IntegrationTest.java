package org.opentripplanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opentripplanner.client.OtpApiClient;
import org.opentripplanner.client.model.Coordinate;
import org.opentripplanner.client.model.FareProductUse;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.parameters.TripPlanParameters;
import org.opentripplanner.client.parameters.TripPlanParameters.SearchDirection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationTest {

  public static final Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);

  public static final Coordinate OSLO_EAST = new Coordinate(59.9132, 10.7692);
  public static final Coordinate OSLO_WEST = new Coordinate(59.9203, 10.6823);
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

    assertEquals(List.of(), leg.fareProducts());
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

  @Test
  public void bikePlan() throws IOException {

    var result =
        client.plan(
            TripPlanParameters.builder()
                .withFrom(OSLO_WEST)
                .withTo(OSLO_EAST)
                .withTime(LocalDateTime.now())
                .withModes(Set.of(RequestMode.BICYCLE))
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
