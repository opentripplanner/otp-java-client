package org.opentripplanner.client.parameters;

public class InputTriangle {
  double safetyFactor;
  double slopeFactor;
  double timeFactor;

  public InputTriangle(double safetyFactor, double slopeFactor, double timeFactor) {
    this.safetyFactor = safetyFactor;
    this.slopeFactor = slopeFactor;
    this.timeFactor = timeFactor;
  }

  public static InputTriangleBuilder builder() {
    return new InputTriangleBuilder();
  }

  public org.opentripplanner.api.types.InputTriangle toGenegerated() {
    return new org.opentripplanner.api.types.InputTriangle(safetyFactor, slopeFactor, timeFactor);
  }
}
