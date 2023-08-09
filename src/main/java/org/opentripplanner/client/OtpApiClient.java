package org.opentripplanner.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
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
import org.opentripplanner.client.model.Coordinate;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.model.TripPlan;
import org.opentripplanner.client.model.VehicleRentalStation;
import org.opentripplanner.client.serialization.ObjectMappers;
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

  public TripPlan plan(Coordinate from, Coordinate to, LocalDateTime time, Set<RequestMode> modes)
      throws IOException, InterruptedException {

    var formattedModes =
        modes.stream()
            .map(m -> "{mode: %s, qualifier: %s}".formatted(m.mode, m.qualifier))
            .collect(Collectors.joining(", "));
    var formattedQuery =
        planQuery.formatted(
            from.lat(),
            from.lon(),
            to.lat(),
            to.lon(),
            formattedModes,
            time.toLocalDate().toString(),
            time.toLocalTime().toString());

    final var jsonNode = sendRequest(formattedQuery);
    var plan = jsonNode.at("/data/plan");
    return mapper.treeToValue(plan, TripPlan.class);
  }

  public List<VehicleRentalStation> vehicleRentalStations()
      throws IOException, InterruptedException {
    var json =
        sendRequest(
            """
              query {
                vehicleRentalStations{
                  name
                  lat
                  lon
                  realtime
                  vehiclesAvailable
                }
              }
                """);

    var type =
        TypeFactory.defaultInstance()
            .constructCollectionType(List.class, VehicleRentalStation.class);

    return mapper.treeToValue(json.at("/data/vehicleRentalStations"), type);
  }

  private JsonNode sendRequest(String formattedQuery) throws IOException, InterruptedException {
    LOG.debug("Sending GraphQL query to {}: {}", graphQlUri, formattedQuery);
    var req =
        HttpRequest.newBuilder(graphQlUri)
            .POST(BodyPublishers.ofString(formattedQuery))
            .header("Content-Type", "application/graphql")
            .build();

    var resp = httpClient.send(req, BodyHandlers.ofString(StandardCharsets.UTF_8));

    var jsonString = resp.body();
    var jsonNode = mapper.readTree(jsonString);
    LOG.info("Received the following JSON: {}", jsonNode.toPrettyString());
    return jsonNode;
  }
}
