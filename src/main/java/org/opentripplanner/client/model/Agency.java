package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Agency(@JsonProperty("gtfsId") String id, String name) {}
