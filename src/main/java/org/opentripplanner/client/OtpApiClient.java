package org.opentripplanner.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.opentripplanner.client.model.Coordinate;
import org.opentripplanner.client.model.Pattern;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.model.Route;
import org.opentripplanner.client.model.TripPlan;
import org.opentripplanner.client.model.VehicleRentalStation;
import org.opentripplanner.client.query.GraphQLQueries;
import org.opentripplanner.client.serialization.ObjectMappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtpApiClient {

  private static final Logger LOG = LoggerFactory.getLogger(OtpApiClient.class);
  private static final String DEFAULT_GRAPHQL_PATH = "/otp/routers/default/index/graphql";

  private final CloseableHttpClient httpClient = HttpClientBuilder.create().build();

  private final URI graphQlUri;
  private final ObjectMapper mapper;

  public OtpApiClient(ZoneId timezone, String baseUrl) {
    this.mapper = ObjectMappers.withTimezone(timezone);
    this.graphQlUri = URI.create(baseUrl + DEFAULT_GRAPHQL_PATH);
  }

  public TripPlan plan(Coordinate from, Coordinate to, LocalDateTime time, Set<RequestMode> modes)
      throws IOException, InterruptedException {

    var planQuery = GraphQLQueries.plan();
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

  public List<Route> routes() throws IOException, InterruptedException {
    var json = sendRequest(GraphQLQueries.routes());
    var type = listType(Route.class);
    return mapper.treeToValue(json.at("/data/routes"), type);
  }

  public List<VehicleRentalStation> vehicleRentalStations()
      throws IOException, InterruptedException {
    var json = sendRequest(GraphQLQueries.vehicleRentalStations());
    var type = listType(VehicleRentalStation.class);
    return mapper.treeToValue(json.at("/data/vehicleRentalStations"), type);
  }

  public List<Pattern> patterns() throws IOException, InterruptedException {
    var json = sendRequest(GraphQLQueries.patterns());
    var type = listType(Pattern.class);
    return mapper.treeToValue(json.at("/data/patterns"), type);
  }

  private static CollectionType listType(Class<?> clazz) {
    return TypeFactory.defaultInstance().constructCollectionType(List.class, clazz);
  }

  private JsonNode sendRequest(String formattedQuery) throws IOException, InterruptedException {
    LOG.debug("Sending GraphQL query to {}: {}", graphQlUri, formattedQuery);

    var body = mapper.createObjectNode();
    body.put("query", formattedQuery);

    var bodyString = mapper.writeValueAsString(body);

    HttpPost httpPost = new HttpPost(graphQlUri);
    var stringEntity = new StringEntity(bodyString, ContentType.APPLICATION_JSON);
    httpPost.setEntity(stringEntity);
    var response = httpClient.execute(httpPost);
    var jsonNode = mapper.readTree(response.getEntity().getContent());

    LOG.debug("Received the following JSON: {}", jsonNode.toPrettyString());
    return jsonNode;
  }
}
