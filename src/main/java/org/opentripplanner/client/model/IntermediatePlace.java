package org.opentripplanner.client.model;

import java.time.OffsetDateTime;

public record IntermediatePlace(String name, OffsetDateTime departureTime, OffsetDateTime arrivalTime, Stop stop) {}
