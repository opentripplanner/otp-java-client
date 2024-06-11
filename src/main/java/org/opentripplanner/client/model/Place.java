package org.opentripplanner.client.model;

import java.util.Optional;

public record Place(
    String name,
    float lon,
    float lat,
    Optional<Stop> stop,
    Optional<VehicleRentalStation> vehicleRentalStation,
    Optional<RentalVehicle> rentalVehicle,
    Optional<VehicleParking> vehicleParking) {}
