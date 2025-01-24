package org.opentripplanner.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.net.URI;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.opentripplanner.client.model.Agency;
import org.opentripplanner.client.model.Pattern;
import org.opentripplanner.client.model.Route;
import org.opentripplanner.client.model.Stop;
import org.opentripplanner.client.model.TripPlan;
import org.opentripplanner.client.model.VehicleRentalStation;
import org.opentripplanner.client.parameters.TripPlanParameters;
import org.opentripplanner.client.query.GraphQLQueries;
import org.opentripplanner.client.serialization.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtpApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(OtpApiClient.class);
  private static final String DEFAULT_GRAPHQL_PATH = "/otp/gtfs/v1";

  private final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

  private final URI graphQlUri;
  private final ObjectMapper mapper;

  public OtpApiClient(ZoneId timezone, String baseUrl) {
    this.mapper = ObjectMappers.withTimezone(timezone);
    this.graphQlUri = URI.create(baseUrl + DEFAULT_GRAPHQL_PATH);
  }

  /**
   * Returns a TripPlan, also known as a routing result.
   *
   * @link <a href="https://docs.opentripplanner.org/api/dev-2.x/graphql-gtfs/queries/plan">OTP API
   *     docs</a>
   */
  public TripPlan plan(TripPlanParameters req) throws IOException {

    var planQuery = GraphQLQueries.plan();
    var formattedModes =
        req.modes().stream()
            .map(m -> "{mode: %s, qualifier: %s}".formatted(m.mode, m.qualifier))
            .collect(Collectors.joining(", "));
    var formattedQuery =
        planQuery.formatted(
            req.fromPlace().toPlaceString(),
            req.toPlace().toPlaceString(),
            formattedModes,
            req.numItineraries(),
            req.time().toLocalDate().toString(),
            req.time().toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString(),
            req.searchDirection().isArriveBy(),
            req.searchWindow().map(sw -> "searchWindow : %d".formatted(sw.toSeconds())).orElse(""),
            req.walkReluctance().map(Object::toString).orElse("null"),
            req.carReluctance().map(Object::toString).orElse("null"),
            req.bikeReluctance().map(Object::toString).orElse("null"),
            req.bikeWalkingReluctance().map(Object::toString).orElse("null"),
            req.wheelchair(),
            req.banned().map(banned -> "banned : %s".formatted(banned)).orElse(""),
            req.optimize(),
            req.triangle().map(triangle -> "triangle : %s".formatted(triangle)).orElse(""));

    final var jsonNode = sendRequest(formattedQuery);
    return deserialize(jsonNode, "/data/plan", TripPlan.class);
  }

  /**
   * Return the list of routes.
   *
   * @link <a href="https://docs.opentripplanner.org/api/dev-2.x/graphql-gtfs/queries/routes">OTP
   *     API docs</a>
   */
  public List<Route> routes() throws IOException {
    var json = sendRequest(GraphQLQueries.routes());
    var type = listType(Route.class);
    return deserializeList(json, type, "/data/routes");
  }

  /**
   * Return the list of vehicle rental stations.
   *
   * @link <a
   *     href="https://docs.opentripplanner.org/api/dev-2.x/graphql-gtfs/queries/vehicleRentalStations">OTP
   *     API docs</a>
   */
  public List<VehicleRentalStation> vehicleRentalStations() throws IOException {
    var json = sendRequest(GraphQLQueries.vehicleRentalStations());
    var type = listType(VehicleRentalStation.class);
    return deserializeList(json, type, "/data/vehicleRentalStations");
  }

  /**
   * Return the list of trip patterns.
   *
   * @link <a href="https://docs.opentripplanner.org/api/dev-2.x/graphql-gtfs/queries/patterns">OTP
   *     API docs</a>
   */
  public List<Pattern> patterns() throws IOException {
    var json = sendRequest(GraphQLQueries.patterns());
    var type = listType(Pattern.class);
    return deserializeList(json, type, "/data/patterns");
  }

  /**
   * Return the list of agencies.
   *
   * @link <a href="https://docs.opentripplanner.org/api/dev-2.x/graphql-gtfs/queries/agencies">OTP
   *     API docs</a>
   */
  public List<Agency> agencies() throws IOException {
    var json = sendRequest(GraphQLQueries.agencies());
    var type = listType(Agency.class);
    return deserializeList(json, type, "/data/agencies");
  }

  /**
   * Returns a Stop with limited information.
   *
   * @link <a href="https://docs.opentripplanner.org/api/dev-2.x/graphql-gtfs/queries/stop">OTP API
   *     docs</a>
   */
  public Stop stop(String gtfsId) throws IOException {

    var stopQuery = GraphQLQueries.stop();
    var formattedQuery = stopQuery.formatted(gtfsId);

    final var jsonNode = sendRequest(formattedQuery);
    return deserialize(jsonNode, "/data/stop", Stop.class);
  }

  /** Use full text search to query for stops. */
  public List<Stop> stopSearch(String nameMask) throws IOException {
    var stopQuery = GraphQLQueries.stops();
    var formattedQuery = stopQuery.formatted(nameMask);

    final var jsonNode = sendRequest(formattedQuery);
    return deserializeList(jsonNode, listType(Stop.class), "/data/stops");
  }

  private <T> T deserialize(JsonNode jsonNode, String path, Class<T> clazz) throws IOException {
    try {
      var plan = jsonNode.at(path);
      return mapper.treeToValue(plan, clazz);
    } catch (IOException e) {
      LOG.error("Could not deserialize response: {}", jsonNode.toPrettyString());
      throw e;
    }
  }

  private <T> List<T> deserializeList(JsonNode json, CollectionType type, String path)
      throws JsonProcessingException {
    return mapper.treeToValue(json.at(path), type);
  }

  private static CollectionType listType(Class<?> clazz) {
    return TypeFactory.defaultInstance().constructCollectionType(List.class, clazz);
  }

  private JsonNode sendRequest(String formattedQuery) throws IOException {
    LOG.debug("Sending GraphQL query to {}: {}", graphQlUri, formattedQuery);

    var body = mapper.createObjectNode();
    body.put("query", formattedQuery);

    var bodyString = mapper.writeValueAsString(body);

    HttpPost httpPost = new HttpPost(graphQlUri);
    var stringEntity = new StringEntity(bodyString, ContentType.APPLICATION_JSON);
    httpPost.setEntity(stringEntity);
    var response = httpClient.execute(httpPost);
    if (response.getCode() != 200) {
      throw new IOException(
          "HTTP request to '%s' returned status code %s".formatted(graphQlUri, response.getCode()));
    }
    var jsonNode = mapper.readTree(response.getEntity().getContent());

    LOG.trace("Received the following JSON: {}", jsonNode.toPrettyString());
    return jsonNode;
  }
}
