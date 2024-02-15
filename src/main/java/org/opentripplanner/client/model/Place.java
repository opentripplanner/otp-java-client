package org.opentripplanner.client.model;

import java.util.Optional;

public record Place(String name, Optional<Stop> stop) {}
