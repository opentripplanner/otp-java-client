package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.leonard.PolylineUtils;
import io.leonard.Position;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;

public record LegGeometry(@JsonProperty("points") String pathString) {

  private static final int DEFAULT_SRID = 4326;

  private List<Position> toListOPositions() {
    return PolylineUtils.decode(pathString, 5);
  }

  /**
   * uses default SRID as defined in <a
   * href="https://gtfs.org/schedule/reference/#field-types">GTFSdoc </a> to create a JTS Linestring
   */
  public LineString toLinestring() {
    return toLinestring(DEFAULT_SRID);
  }

  /** create a JTS Linestring by a passed SRID */
  public LineString toLinestring(int SRID) {
    Coordinate[] coordinates =
        toListOPositions().stream()
            .map(position -> new Coordinate(position.getLongitude(), position.getLatitude()))
            .toArray(Coordinate[]::new);

    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

    return (geometryFactory.createLineString(coordinates));
  }
}
