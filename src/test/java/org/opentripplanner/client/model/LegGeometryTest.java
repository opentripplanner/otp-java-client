package org.opentripplanner.client.model;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

class LegGeometryTest {

  private static final String GOOGLE_ENCODED = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";

  @Test
  void encoding() {
    var geom = new LegGeometry(GOOGLE_ENCODED);
    assertThat(geom.toGoogleEncoding()).isEqualTo(GOOGLE_ENCODED);
    assertThat(geom.toLinestring().getCoordinates()).hasLength(3);
    assertThat(geom.toPositions()).hasSize(3);

    var first = geom.toLinestring().getCoordinates()[0];
    assertThat(first.y).isEqualTo(38.5);
    assertThat(first.x).isEqualTo(-120.2);
  }
}
