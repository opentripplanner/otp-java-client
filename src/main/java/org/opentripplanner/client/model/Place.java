package org.opentripplanner.client.model;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public sealed interface Place
    permits CoordinatePlace, RentalVehicle, Stop, VehicleParking, VehicleRentalStation {
  float lon();

  float lat();

  /** create a JTS Geometry */
  public default org.locationtech.jts.geom.Coordinate coordinate() {
    return new Coordinate(lon(), lat());
  }

  /**
   * Return the geometry as JTF Point which defaults to SRID as defined in <a
   * href="https://gtfs.org/schedule/reference/#field-types">GTFSdoc </a>.
   */
  public default Point point() {
    return point(4326);
  }

  /** creates a JTS Point by a passed SRID */
  public default Point point(int SRID) {
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);
    return geometryFactory.createPoint(coordinate());
  }
}
