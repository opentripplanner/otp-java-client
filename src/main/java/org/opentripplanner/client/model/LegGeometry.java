package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.leonard.PolylineUtils;
import io.leonard.Position;
import java.util.List;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.PrecisionModel;

public final class LegGeometry {

  private static final int DEFAULT_SRID = 4326;
  private final String googleEncoded;

  public LegGeometry(@JsonProperty("points") String googleEncoded) {
    this.googleEncoded = googleEncoded;
  }

  /** Return the as a list of points. */
  public List<Position> toPositions() {
    return PolylineUtils.decode(googleEncoded, 5);
  }

  /**
   * Return the geometry as JTF LineString which defaults to SRID as defined in <a
   * href="https://gtfs.org/schedule/reference/#field-types">GTFSdoc </a>.
   */
  public LineString toLinestring() {
    return toLinestring(DEFAULT_SRID);
  }

  /** create a JTS Linestring by a passed SRID */
  public LineString toLinestring(int SRID) {
    Coordinate[] coordinates =
        toPositions().stream()
            .map(position -> new Coordinate(position.getLongitude(), position.getLatitude()))
            .toArray(Coordinate[]::new);

    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);

    return geometryFactory.createLineString(coordinates);
  }

  /** Return the geometry as a raw Google encoded polyline. */
  public String toGoogleEncoding() {
    return googleEncoded;
  }
}
