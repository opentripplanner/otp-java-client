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

  public static final Coordinate BAYRISCHER_PLATZ = new Coordinate(52.4885, 13.3398);
  public static final Coordinate ALEXANDERPLATZ = new Coordinate(52.5211, 13.4106);
  public static OtpApiClient client = new OtpApiClient(ZoneId.of("Europe/Berlin"), "api.bbnavi.de");

  @Test
  public void route() throws IOException, InterruptedException {

    var result =
        client.plan(
            BAYRISCHER_PLATZ, ALEXANDERPLATZ, LocalDateTime.now(), Set.of(RequestMode.TRANSIT));

    LOG.info("Received {}", result);

    assertNotNull(result.itineraries().get(0).legs().get(0).startTime());
  }

  @Test
  public void rentalStations() throws IOException, InterruptedException {

    var result = client.vehicleRentalStations();

    LOG.info("Received {}", result);

    assertFalse(result.isEmpty());
  }
}
