package org.opentripplanner.client.model;

import java.util.Optional;

public record Stop(String name, String gtfsId, Optional<String> code) {}
