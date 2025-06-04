package org.opentripplanner.client.model;

import java.util.Optional;

public record FareProductUse(String id, FareProduct product) {

  public record FareProduct(
      String id,
      String name,
      Money price,
      Optional<RiderCategory> riderCategory,
      Optional<FareProduct.FareMedium> medium) {

    public record RiderCategory(String id, String name) {}

    public record FareMedium(String id, String name) {}
  }
}
