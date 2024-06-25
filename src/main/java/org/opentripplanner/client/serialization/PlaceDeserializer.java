package org.opentripplanner.client.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Optional;
import org.opentripplanner.client.model.CoordinatePlace;
import org.opentripplanner.client.model.ParentStation;
import org.opentripplanner.client.model.ParkingCapacity;
import org.opentripplanner.client.model.RentalVehicle;
import org.opentripplanner.client.model.RentalVehicleType;
import org.opentripplanner.client.model.Stop;
import org.opentripplanner.client.model.VehicleParking;
import org.opentripplanner.client.model.VehicleRentalStation;

public class PlaceDeserializer extends JsonDeserializer {
  @Override
  public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonNode place = p.getCodec().readTree(p);

    if (place.hasNonNull("stop")) {
      return deserializeStop(ctxt, place);
    }

    if (place.hasNonNull("vehicleRentalStation")) {
      return deserializeRentalStation(place);
    }

    if (place.hasNonNull("rentalVehicle")) {
      return deserializeRentalVehicle(ctxt, place);
    }

    if (place.hasNonNull("vehicleParking")) {
      return deserializeVehicleParking(ctxt, place);
    }

    return deserializeCoordinatePlace(place);
  }

  private static Stop deserializeStop(DeserializationContext ctxt, JsonNode place)
      throws IOException {
    JsonNode stop = place.get("stop");
    return new Stop(
        stop.get("name").textValue(),
        stop.get("gtfsId").textValue(),
        place.get("lon").floatValue(),
        place.get("lat").floatValue(),
        Optional.ofNullable(stop.get("code").textValue()),
        Optional.ofNullable(stop.get("zoneId").textValue()),
        ctxt.readTreeAsValue(stop.get("parentStation"), ParentStation.class));
  }

  private static VehicleRentalStation deserializeRentalStation(JsonNode place) {
    JsonNode rentalStation = place.get("vehicleRentalStation");
    return new VehicleRentalStation(
        rentalStation.get("stationId").textValue(),
        rentalStation.get("name").textValue(),
        place.get("lat").floatValue(),
        place.get("lon").floatValue(),
        rentalStation.get("network").textValue());
  }

  private static RentalVehicle deserializeRentalVehicle(DeserializationContext ctxt, JsonNode place)
      throws IOException {
    JsonNode rentalVehicle = place.get("rentalVehicle");
    return new RentalVehicle(
        rentalVehicle.get("vehicleId").textValue(),
        place.get("lat").floatValue(),
        place.get("lon").floatValue(),
        Optional.ofNullable(rentalVehicle.get("name").textValue()),
        rentalVehicle.get("network").textValue(),
        Optional.ofNullable(
            rentalVehicle.hasNonNull("vehicleType")
                ? ctxt.readTreeAsValue(rentalVehicle.get("vehicleType"), RentalVehicleType.class)
                : null));
  }

  private VehicleParking deserializeVehicleParking(DeserializationContext ctxt, JsonNode place)
      throws IOException {
    JsonNode vehicleParking = place.get("vehicleParking");
    return new VehicleParking(
        vehicleParking.get("vehicleParkingId").textValue(),
        vehicleParking.get("name").textValue(),
        place.get("lat").floatValue(),
        place.get("lon").floatValue(),
        Optional.ofNullable(
            vehicleParking.hasNonNull("capacity")
                ? ctxt.readTreeAsValue(vehicleParking.get("capacity"), ParkingCapacity.class)
                : null));
  }

  private static CoordinatePlace deserializeCoordinatePlace(JsonNode place) {
    return new CoordinatePlace(place.get("lon").floatValue(), place.get("lat").floatValue());
  }
}
