package org.opentripplanner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opentripplanner.client.OtpApiClient;
import org.opentripplanner.client.model.Coordinate;
import org.opentripplanner.client.model.RequestMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationTest {

  public static final Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);

  public static final Coordinate KARLSRUD = new Coordinate(59.8901, 10.7819);
  public static final Coordinate ULLERN = new Coordinate(59.9256, 10.6569);
  public static OtpApiClient client =
      new OtpApiClient(ZoneId.of("Europe/Berlin"), "https://otp2debug.entur.org");

  @Test
  public void route() throws IOException, InterruptedException {

    var result = client.plan(KARLSRUD, ULLERN, LocalDateTime.now(), Set.of(RequestMode.TRANSIT));

    LOG.info("Received {} itineraries", result.itineraries().size());

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());
  }

  @Test
  public void rentalStations() throws IOException, InterruptedException {

    var result = client.vehicleRentalStations();

    LOG.info("Received {} rental stations", result.size());

    assertFalse(result.isEmpty());
  }

  @Test
  public void routes() throws IOException, InterruptedException {

    var result = client.routes();

    LOG.info("Received {} routes", result.size());

    assertFalse(result.isEmpty());
  }

  @Test
  public void patterns() throws IOException, InterruptedException {

    var result = client.patterns();

    LOG.info("Received {} patterns", result.size());

    assertFalse(result.isEmpty());
  }
}
