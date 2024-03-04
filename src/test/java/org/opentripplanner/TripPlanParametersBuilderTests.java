package org.opentripplanner;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opentripplanner.StationParameters.OSLO_EAST;
import static org.opentripplanner.StationParameters.OSLO_LUFTHAVN_ID;
import static org.opentripplanner.StationParameters.OSLO_S_ID;
import static org.opentripplanner.StationParameters.OSLO_WEST;

import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opentripplanner.client.model.RequestMode;
import org.opentripplanner.client.parameters.TripPlanParameters;

public class TripPlanParametersBuilderTests {
  @Test
  public void planWithoutFromGpsOrFromPlace() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                TripPlanParameters.builder()
                    .withTime(LocalDateTime.now())
                    .withModes(Set.of(RequestMode.TRANSIT))
                    .build());

    assertTrue(exception.getMessage().contains("fromPlace"));
  }

  @Test
  public void planWithoutToGpsOrToPlace() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                TripPlanParameters.builder()
                    .withFromPlace(OSLO_S_ID)
                    .withTime(LocalDateTime.now())
                    .withModes(Set.of(RequestMode.TRANSIT))
                    .build());

    assertTrue(exception.getMessage().contains("toPlace"));
  }

  @Test
  public void planWithFromGpsAndFromPlace() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                TripPlanParameters.builder()
                    .withFrom(OSLO_WEST)
                    .withFromPlace(OSLO_LUFTHAVN_ID)
                    .withTo(OSLO_EAST)
                    .withTime(LocalDateTime.now())
                    .withModes(Set.of(RequestMode.TRANSIT))
                    .build());

    assertTrue(exception.getMessage().contains("fromPlace"));
  }

  @Test
  public void planWithToGpsOrToPlace() {
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                TripPlanParameters.builder()
                    .withFrom(OSLO_WEST)
                    .withTo(OSLO_EAST)
                    .withToPlace(OSLO_S_ID)
                    .withTime(LocalDateTime.now())
                    .withModes(Set.of(RequestMode.TRANSIT))
                    .build());

    assertTrue(exception.getMessage().contains("toPlace"));
  }
}
