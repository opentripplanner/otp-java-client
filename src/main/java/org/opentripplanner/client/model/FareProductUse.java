package org.opentripplanner.client.model;

import jakarta.annotation.Nullable;

public record FareProductUse(String id, FareProduct product) {

  public record FareProduct(
      String id,
      String name,
      Money price,
      @Nullable FareProduct.RiderCategory riderCategory,
      @Nullable FareProduct.FareMedium medium) {

    public record RiderCategory(String id, String name) {}

    public record FareMedium(String id, String name) {}
  }
}
