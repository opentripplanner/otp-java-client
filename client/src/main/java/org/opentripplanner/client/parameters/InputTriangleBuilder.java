package org.opentripplanner.client.parameters;

public class InputTriangleBuilder {
  private float safetyFactor;
  private float slopeFactor;
  private float timeFactor;

  public InputTriangleBuilder withSafetyFactor(float safetyFactor) {
    this.safetyFactor = safetyFactor;
    return this;
  }

  public InputTriangleBuilder withSlopeFactor(float slopeFactor) {
    this.slopeFactor = slopeFactor;
    return this;
  }

  public InputTriangleBuilder withTimeFactor(float timeFactor) {
    this.timeFactor = timeFactor;
    return this;
  }

  public InputTriangleBuilder copy() {
    return new InputTriangleBuilder()
        .withSafetyFactor(this.safetyFactor)
        .withSlopeFactor(this.slopeFactor)
        .withTimeFactor(this.timeFactor);
  }

  public InputTriangle build() {
    return new InputTriangle(safetyFactor, slopeFactor, timeFactor);
  }
}
