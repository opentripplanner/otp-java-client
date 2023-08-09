package org.opentripplanner;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opentripplanner.otp.OtpApiClient;
import org.opentripplanner.otp.model.Coordinate;
import org.opentripplanner.otp.model.ModeInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrationTest {

  public static final Logger LOG = LoggerFactory.getLogger(IntegrationTest.class);

  public static final Coordinate BAYRISCHER_PLATZ = new Coordinate(52.4885, 13.3398);
  public static final Coordinate ALEXANDERPLATZ = new Coordinate(52.5211, 13.4106);

  @Test
  public void test() throws IOException, InterruptedException {

    var client = new OtpApiClient(ZoneId.of("Europe/Berlin"), "api.bbnavi.de");
    var result =
        client.plan(
            BAYRISCHER_PLATZ, ALEXANDERPLATZ, LocalDateTime.now(), Set.of(ModeInput.TRANSIT));

    LOG.info("Received {}", result);

    assertNotNull(result.get(0).legs().get(0).startTime());
  }
}
