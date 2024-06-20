package org.opentripplanner.client.model;

import java.util.Optional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public record Place(
    String name,
    float lon,
    float lat,
    Optional<Stop> stop,
    Optional<VehicleRentalStation> vehicleRentalStation,
    Optional<RentalVehicle> rentalVehicle,
    Optional<VehicleParking> vehicleParking) {

  private static final int DEFAULT_SRID = 4326;

  /** create a JTS Geometry */
  public Coordinate coordinate() {
    return new Coordinate(lon, lat);
  }

  /**
   * Return the geometry as JTF Point which defaults to SRID as defined in <a
   * href="https://gtfs.org/schedule/reference/#field-types">GTFSdoc </a>.
   */
  public Point point() {
    return point(DEFAULT_SRID);
  }

  /** creates a JTS Point by a passed SRID */
  public Point point(int SRID) {
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), SRID);
    return geometryFactory.createPoint(coordinate());
  }
}
