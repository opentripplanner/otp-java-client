package org.opentripplanner.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ParentStation(@JsonProperty("gtfsId") String id) {}
