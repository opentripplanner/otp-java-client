package org.opentripplanner.client.parameters;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TripPlanParametersBuilderTest {

  @Test
  void copy() {
    var builder = new TripPlanParametersBuilder().copy();
  }
}
