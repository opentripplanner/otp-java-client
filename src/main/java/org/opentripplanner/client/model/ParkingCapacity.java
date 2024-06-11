package org.opentripplanner.client.model;

import java.util.Optional;

public record ParkingCapacity(Optional<Integer> bicycleSpaces, Optional<Integer> carSpaces) {}
