package org.opentripplanner.client.parameters;

public class InputTriangle {
  float safetyFactor;
  float slopeFactor;
  float timeFactor;

  public InputTriangle(float safetyFactor, float slopeFactor, float timeFactor) {
    this.safetyFactor = safetyFactor;
    this.slopeFactor = slopeFactor;
    this.timeFactor = timeFactor;
  }

  public static InputTriangleBuilder builder() {
    return new InputTriangleBuilder();
  }

  @Override
  public String toString() {
    String safetyFactorString = String.format("safetyFactor: %f", safetyFactor);
    String slopeFactorString = String.format("slopeFactor: %f", slopeFactor);
    String timeFactorString = String.format("timeFactor: %f", timeFactor);

    return String.format("{%s %s %s}", safetyFactorString, slopeFactorString, timeFactorString);
  }
}
