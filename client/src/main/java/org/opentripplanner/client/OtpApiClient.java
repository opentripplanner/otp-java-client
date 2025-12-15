package org.opentripplanner.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLRequest;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.opentripplanner.api.types.AgencyResponseProjection;
import org.opentripplanner.api.types.DefaultFareProductResponseProjection;
import org.opentripplanner.api.types.FareMediumResponseProjection;
import org.opentripplanner.api.types.FareProductResponseProjection;
import org.opentripplanner.api.types.FareProductUseResponseProjection;
import org.opentripplanner.api.types.GeometryResponseProjection;
import org.opentripplanner.api.types.ItineraryResponseProjection;
import org.opentripplanner.api.types.LegResponseProjection;
import org.opentripplanner.api.types.PlaceResponseProjection;
import org.opentripplanner.api.types.PlanQueryRequest;
import org.opentripplanner.api.types.PlanResponseProjection;
import org.opentripplanner.api.types.RiderCategoryResponseProjection;
import org.opentripplanner.api.types.Route;
import org.opentripplanner.api.types.RouteResponseProjection;
import org.opentripplanner.api.types.RoutesQueryRequest;
import org.opentripplanner.api.types.StopResponseProjection;
import org.opentripplanner.api.types.TripResponseProjection;
import org.opentripplanner.client.model.Agency;
import org.opentripplanner.client.model.Alert;
import org.opentripplanner.client.model.Pattern;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.model.Stop;
import org.opentripplanner.client.model.TripPlan;
import org.opentripplanner.client.model.VehicleRentalStation;
import org.opentripplanner.client.parameters.InputTriangle;
import org.opentripplanner.client.parameters.TripPlanParameters;
import org.opentripplanner.client.query.GraphQLQueries;
import org.opentripplanner.client.serialization.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtpApiClient {

  public static final AgencyResponseProjection AGENCY_PROJECTION =
      new AgencyResponseProjection().gtfsId().name();
  public static final RouteResponseProjection ROUTE_PROJECTION =
      new RouteResponseProjection()
          .id()
          .gtfsId()
          .longName()
          .shortName()
          .bikesAllowed()
          .mode()
          .agency(AGENCY_PROJECTION)
          .typename();
  private static final Logger LOG = LoggerFactory.getLogger(OtpApiClient.class);
  private static final String DEFAULT_GRAPHQL_PATH = "/otp/gtfs/v1";
  public static final StopResponseProjection STOP_PROJECTION =
      new StopResponseProjection().gtfsId().name().code();
  public static final PlaceResponseProjection PLACE_PROJECTION =
      new PlaceResponseProjection()
          .name()
          .departureTime()
          .arrivalTime()
          .stop(STOP_PROJECTION)
          .lat()
          .lon();

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
    var r = new PlanQueryRequest();
    r.setFromPlace(req.fromPlace().toPlaceString());
    r.setToPlace(req.toPlace().toPlaceString());
    r.setTransportModes(req.modes().stream().map(RequestMode::toTransportMode).toList());
    r.setNumItineraries(req.numItineraries());
    r.setTime(req.time().toLocalTime().truncatedTo(ChronoUnit.SECONDS).toString());
    r.setDate(req.time().toLocalDate().toString());
    r.setArriveBy(req.searchDirection().isArriveBy());
    r.setSearchWindow(req.searchWindow().map(Duration::toSeconds).orElse(null));
    r.setWalkReluctance(req.walkReluctance().orElse(null));
    r.setCarReluctance(req.carReluctance().orElse(null));
    r.setBikeReluctance(req.bikeReluctance().orElse(null));
    r.setBikeWalkingReluctance(req.bikeWalkingReluctance().orElse(null));
    r.setWheelchair(req.wheelchair());
    // TODO: implement banning
    r.setOptimize(req.optimize());
    r.setTriangle(req.triangle().map(InputTriangle::toGenegerated).orElse(null));

    final LegResponseProjection legProjection =
        new LegResponseProjection()
            .accessibilityScore()
            .mode()
            .startTime()
            .endTime()
            .headsign()
            .from(PLACE_PROJECTION)
            .to(PLACE_PROJECTION)
            .intermediatePlaces(PLACE_PROJECTION)
            .agency(AGENCY_PROJECTION)
            .trip(new TripResponseProjection().gtfsId().tripHeadsign())
            .route(ROUTE_PROJECTION)
            .legGeometry(new GeometryResponseProjection().points().length())
            .interlineWithPreviousLeg()
            .duration()
            .fareProducts(
                new FareProductUseResponseProjection()
                    .product(
                        new FareProductResponseProjection()
                            .name()
                            .riderCategory(new RiderCategoryResponseProjection().all$())
                            .medium(new FareMediumResponseProjection().all$())
                            .onDefaultFareProduct(
                                new DefaultFareProductResponseProjection().all$())));
    var tripPlanProjection =
        new PlanResponseProjection()
            .itineraries(
                new ItineraryResponseProjection()
                    .accessibilityScore()
                    .duration()
                    .legs(legProjection));

    var graphQLRequest = new GraphQLRequest(r, tripPlanProjection);

    final var jsonNode = sendRequest(graphQLRequest);
    return deserialize(jsonNode, "/data/plan", TripPlan.class);
  }

  /**
   * Return the list of routes.
   *
   * @link <a href="https://docs.opentripplanner.org/api/dev-2.x/graphql-gtfs/queries/routes">OTP
   *     API docs</a>
   */
  public List<Route> routes() throws IOException {
    var req = new RoutesQueryRequest();

    var graphQLRequest = new GraphQLRequest(req, ROUTE_PROJECTION);

    var result = sendRequest(graphQLRequest.toHttpJsonBody());
    return deserializeList(result, listType(Route.class), "/data/routes");
  }

  /**
   * Return the list of vehicle rental stations.
   *
   * @link <a
   *     href="https://docs.opentripplanner.org/api/dev-2.x/graphql-gtfs/queries/vehicleRentalStations">OTP
   *     API docs</a>
   */
  public List<VehicleRentalStation> vehicleRentalStations() throws IOException {
    var json = sendWrappedRequest(GraphQLQueries.vehicleRentalStations());
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
    var json = sendWrappedRequest(GraphQLQueries.patterns());
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
    var json = sendWrappedRequest(GraphQLQueries.agencies());
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

    final var jsonNode = sendWrappedRequest(formattedQuery);
    return deserialize(jsonNode, "/data/stop", Stop.class);
  }

  /** Use full text search to query for stops. */
  public List<Stop> stopSearch(String nameMask) throws IOException {
    var stopQuery = GraphQLQueries.stops();
    var formattedQuery = stopQuery.formatted(nameMask);

    final var jsonNode = sendWrappedRequest(formattedQuery);
    return deserializeList(jsonNode, listType(Stop.class), "/data/stops");
  }

  /** Get all alerts. */
  public List<Alert> alerts() throws IOException {
    var query = GraphQLQueries.alerts();
    final var jsonNode = sendWrappedRequest(query);
    return deserializeList(jsonNode, listType(Alert.class), "/data/alerts");
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

  private JsonNode sendRequest(GraphQLRequest req) throws IOException {
    return sendRequest(req.toHttpJsonBody());
  }

  private JsonNode sendRequest(String formattedQuery) throws IOException {
    LOG.debug("Sending GraphQL query to {}: {}", graphQlUri, formattedQuery);

    HttpPost httpPost = new HttpPost(graphQlUri);
    var stringEntity = new StringEntity(formattedQuery, ContentType.APPLICATION_JSON);
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

  @Deprecated
  private JsonNode sendWrappedRequest(String formattedQuery) throws IOException {
    var body = mapper.createObjectNode();
    body.put("query", formattedQuery);
    var bodyString = mapper.writeValueAsString(body);
    return sendRequest(bodyString);
  }
}
