package org.opentripplanner.otp;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.opentripplanner.otp.model.Coordinate;
import org.opentripplanner.otp.model.Mode;
import org.opentripplanner.otp.model.ModeInput;
import org.opentripplanner.otp.model.TripPlan;
import org.opentripplanner.otp.model.TripPlan.Itinerary;
import org.opentripplanner.otp.serialization.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtpApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(OtpApiClient.class);

  private final HttpClient httpClient = HttpClient.newHttpClient();

  private final URI graphQlUri;
  private final ObjectMapper mapper;

  public OtpApiClient(ZoneId timezone, String hostName) {
    this.mapper = ObjectMappers.withTimezone(timezone);
    this.graphQlUri = URI.create("https://" + hostName + "/otp/routers/default/index/graphql");
  }

  private static final String planQuery =
      """
  query {
    plan(
      from: {lat: %s, lon: %s}
      to: {lat: %s, lon: %s}
      transportModes: [ %s ]
      numItineraries: 5
      date: "%s"
      time: "%s"
    ) {
      itineraries {
        startTime
        endTime
        legs {
          startTime
          endTime
          from {
            name
          }
          to {
            name
          }
          mode
          route {
            shortName
            longName
            agency {
              name
            }
          }
          duration
          distance
          intermediatePlaces {
            name
            departureTime
            arrivalTime
          }
        }
      }
    }
  }
""";

  public List<Itinerary> plan(Coordinate from, Coordinate to, LocalDateTime time, Set<ModeInput> modes)
      throws IOException, InterruptedException {

    var formattedModes =
        modes.stream().map("{mode: %s}"::formatted).collect(Collectors.joining(", "));
    var formattedQuery =
        planQuery.formatted(
            from.lat(),
            from.lon(),
            to.lat(),
            to.lon(),
            formattedModes,
            time.toLocalDate().toString(),
            time.toLocalTime().toString());

    LOG.debug("Sending GraphQL query to {}: {}", graphQlUri, formattedQuery);

    var req =
        HttpRequest.newBuilder(graphQlUri)
            .POST(BodyPublishers.ofString(formattedQuery))
            .header("Content-Type", "application/graphql")
            .build();

    var resp = httpClient.send(req, BodyHandlers.ofString(StandardCharsets.UTF_8));

    var jsonString = resp.body();
    var jsonNode = mapper.readTree(jsonString);
    LOG.debug("Received the following JSON: {}", jsonNode.toPrettyString());

    var plan = jsonNode.at("/data/plan");

    var tripPlan = mapper.treeToValue(plan, TripPlan.class);

    return tripPlan.itineraries();
  }
}
