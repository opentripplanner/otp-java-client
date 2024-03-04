package org.opentripplanner.client.model;

public record StopId(String id) implements PlaceParameter {
    @Override
    public String toPlanParameter(String direction) {
        return String.format("%sPlace: \"%s\"", direction, id());
    }
}
