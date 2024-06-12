package org.opentripplanner.client.query;

import java.util.Objects;
import java.util.Scanner;

public class GraphQLQueries {

  public static String vehicleRentalStations() {
    return loadQuery("vehicleRentalStations");
  }

  public static String plan() {
    return loadQuery("plan");
  }

  public static String routes() {
    return loadQuery("routes");
  }

  public static String patterns() {
    return loadQuery("patterns");
  }

  public static String agencies() {
    return loadQuery("agencies");
  }

  public static String stop() {
    return loadQuery("stop");
  }

  private static String loadQuery(String name) {
    var is =
        GraphQLQueries.class
            .getClassLoader()
            .getResourceAsStream("queries/%s.graphql".formatted(name));
    Objects.requireNonNull(is);
    var s = new Scanner(is).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }
}
