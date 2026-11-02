package org.opentripplanner.assertions;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.opentripplanner.api.types.Route;
import org.opentripplanner.client.model.Agency;
import org.opentripplanner.client.model.Currency;
import org.opentripplanner.client.model.FareProductUse;
import org.opentripplanner.client.model.Itinerary;
import org.opentripplanner.client.model.Leg;
import org.opentripplanner.client.model.LegGeometry;
import org.opentripplanner.client.model.LegMode;
import org.opentripplanner.client.model.Money;
import org.opentripplanner.client.model.Place;
import org.opentripplanner.client.model.Stop;
import org.opentripplanner.client.model.Trip;
import org.opentripplanner.client.model.TripPlan;

class ItineraryExpectationTest {

  private static final OffsetDateTime START = OffsetDateTime.parse("2026-01-01T08:00:00Z");
  private static final String GEOMETRY = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";

  @Test
  void singleLegMatchSuccess() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of())));

    assertDoesNotThrow(
        () ->
            new ItineraryExpectation()
                .withLeg(new TransitLegExpectation().withMode("BUS").withRouteShortName("10"))
                .evaluate(plan)
                .assertMatches());
  }

  @Test
  void nonTransitLegsAreIgnored() {
    TripPlan plan =
        tripPlan(
            itinerary(
                walkLeg(Duration.ofMinutes(10)),
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of()),
                walkLeg(Duration.ofMinutes(10))));

    assertDoesNotThrow(
        () ->
            new ItineraryExpectation()
                .withTransitLegMatching(TransitLegMatching.EXACT_ORDERED)
                .withLeg(new TransitLegExpectation().withRouteShortName("10"))
                .evaluate(plan)
                .assertMatches());
  }

  @Test
  void multiLegMatchSuccess() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of()),
                transitLeg("1", "Line 1", LegMode.TRAM, Duration.ofMinutes(15), List.of())));

    assertDoesNotThrow(
        () ->
            new ItineraryExpectation()
                .withLeg(new TransitLegExpectation().withRouteShortName("10"))
                .withLeg(new TransitLegExpectation().withRouteShortName("1"))
                .evaluate(plan)
                .assertMatches());
  }

  @Test
  void exactTransitLegsMustMatchInOrder() {
    TripPlan plan =
        tripPlan(
            itinerary(
                walkLeg(Duration.ofMinutes(5)),
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of()),
                transitLeg("1", "Line 1", LegMode.TRAM, Duration.ofMinutes(15), List.of()),
                walkLeg(Duration.ofMinutes(5))));

    assertDoesNotThrow(
        () ->
            new ItineraryExpectation()
                .withTransitLegMatching(TransitLegMatching.EXACT_ORDERED)
                .withLeg(new TransitLegExpectation().withRouteShortName("10"))
                .withLeg(new TransitLegExpectation().withRouteShortName("1"))
                .evaluate(plan)
                .assertMatches());

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryExpectation()
                    .withTransitLegMatching(TransitLegMatching.EXACT_ORDERED)
                    .withLeg(new TransitLegExpectation().withRouteShortName("1"))
                    .withLeg(new TransitLegExpectation().withRouteShortName("10"))
                    .evaluate(plan)
                    .assertMatches());

    assertThat(error.getMessage()).contains("Transit leg at position 1 does not match");
    assertThat(error.result().matchingMode()).isEqualTo(TransitLegMatching.EXACT_ORDERED);
  }

  @Test
  void nonStrictTransitLegsRemainUnordered() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of()),
                transitLeg("1", "Line 1", LegMode.TRAM, Duration.ofMinutes(15), List.of())));

    assertDoesNotThrow(
        () ->
            new ItineraryExpectation()
                .withLeg(new TransitLegExpectation().withRouteShortName("1"))
                .withLeg(new TransitLegExpectation().withRouteShortName("10"))
                .evaluate(plan)
                .assertMatches());
  }

  @Test
  void matchesBoardingAndAlightingStopsByCodeOrGtfsId() {
    Leg leg =
        transitLeg(
            "10",
            "Route 10",
            LegMode.BUS,
            Duration.ofMinutes(20),
            List.of(),
            place("Board", "feed:board", "BOARD"),
            place("Alight", "feed:alight", "ALIGHT"),
            false);
    TripPlan plan = tripPlan(itinerary(leg));

    assertDoesNotThrow(
        () ->
            new ItineraryExpectation()
                .withLeg(
                    new TransitLegExpectation()
                        .withBoardingStopCode("BOARD")
                        .withBoardingStopGtfsId("feed:board")
                        .withAlightingStopCode("ALIGHT")
                        .withAlightingStopGtfsId("feed:alight"))
                .evaluate(plan)
                .assertMatches());

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryExpectation()
                    .withLeg(
                        new TransitLegExpectation()
                            .withBoardingStopCode("ALIGHT")
                            .withAlightingStopGtfsId("feed:board"))
                    .evaluate(plan)
                    .assertMatches());

    assertThat(error.getMessage()).contains("boarding stop code '[ALIGHT]'");
    assertThat(error.getMessage()).contains("alighting stop GTFS ID '[feed:board]'");
  }

  @Test
  void exactMatchingChecksInterliningAtTheExpectedTransition() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg(
                    "10",
                    "Route 10",
                    LegMode.BUS,
                    Duration.ofMinutes(10),
                    List.of(),
                    place("A"),
                    place("B"),
                    false),
                transitLeg(
                    "10",
                    "Route 10",
                    LegMode.BUS,
                    Duration.ofMinutes(10),
                    List.of(),
                    place("B"),
                    place("C"),
                    true)));

    assertDoesNotThrow(
        () ->
            new ItineraryExpectation()
                .withTransitLegMatching(TransitLegMatching.EXACT_ORDERED)
                .withLeg(new TransitLegExpectation().withRouteShortName("10"))
                .withLeg(
                    new TransitLegExpectation()
                        .withRouteShortName("10")
                        .withInterlinedWithPreviousLeg(true))
                .evaluate(plan)
                .assertMatches());

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryExpectation()
                    .withTransitLegMatching(TransitLegMatching.EXACT_ORDERED)
                    .withLeg(
                        new TransitLegExpectation()
                            .withRouteShortName("10")
                            .withInterlinedWithPreviousLeg(true))
                    .withLeg(new TransitLegExpectation().withRouteShortName("10"))
                    .evaluate(plan)
                    .assertMatches());

    assertThat(error.getMessage()).contains("Transit leg at position 1 does not match");
    assertThat(error.getMessage()).contains("interlined with previous leg");
  }

  @Test
  void strictMatchingRejectsExtraTransitLegs() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of()),
                transitLeg("1", "Line 1", LegMode.TRAM, Duration.ofMinutes(15), List.of())));

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryExpectation()
                    .withTransitLegMatching(TransitLegMatching.EXACT_ORDERED)
                    .withLeg(new TransitLegExpectation().withRouteShortName("10"))
                    .evaluate(plan)
                    .assertMatches());

    assertThat(error.getMessage()).contains("additional transit legs");
    assertThat(error.result().itineraryResults()).hasSize(1);
    assertThat(error.result().itineraryResults().get(0).extraMatches()).hasSize(1);
  }

  @Test
  void partialMatchesAndErrorDetailsAreIncluded() {
    TripPlan plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of())));

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryExpectation()
                    .withLeg(new TransitLegExpectation().withRouteShortName("10").withMode("TRAM"))
                    .evaluate(plan)
                    .assertMatches());

    assertThat(error.result().itineraryResults()).hasSize(1);
    ItineraryMatchResult result = error.result().itineraryResults().get(0);
    assertThat(result.legResults()).hasSize(1);
    var requirement = result.legResults().getFirst();
    assertThat(requirement.status()).isEqualTo(LegRequirementResult.Status.CRITERIA_MISMATCH);
    assertThat(requirement.candidates()).hasSize(1);
    assertThat(requirement.candidates().getFirst().matchingCriteria())
        .containsExactly("route '[10]'");
    assertThat(requirement.candidates().getFirst().missingCriteria()).containsExactly("mode TRAM");
  }

  @Test
  void withFarePriceMatchesFareWithSpecifiedPriceRiderCategoryAndMedium() {
    List<FareProductUse> fares = List.of(fare(2.75f, "orca:regular", "orca:cash"));
    TripPlan plan =
        tripPlan(itinerary(transitLeg("E", "E Line", LegMode.BUS, Duration.ofMinutes(12), fares)));

    assertDoesNotThrow(
        () ->
            new ItineraryExpectation()
                .withLeg(
                    new TransitLegExpectation()
                        .withRouteShortName("E")
                        .withFarePrice(2.75f, "orca:regular", "orca:cash"))
                .evaluate(plan)
                .assertMatches());
  }

  @Test
  void withFarePriceReportsMismatchedFare() {
    List<FareProductUse> fares = List.of(fare(2.75f, "orca:regular", "orca:cash"));
    TripPlan plan =
        tripPlan(itinerary(transitLeg("E", "E Line", LegMode.BUS, Duration.ofMinutes(12), fares)));

    ItineraryAssertionError error =
        assertThrows(
            ItineraryAssertionError.class,
            () ->
                new ItineraryExpectation()
                    .withLeg(
                        new TransitLegExpectation()
                            .withRouteShortName("E")
                            .withFarePrice(3.00f, "orca:regular", "orca:cash"))
                    .evaluate(plan)
                    .assertMatches());

    String expectedFareCriterion = "fare $3.00 (rider category orca:regular, medium orca:cash)";
    assertThat(error.result().expectedLegs())
        .containsExactly(List.of("route '[E]'", expectedFareCriterion));
    assertThat(error.result().itineraryResults()).hasSize(1);
    var requirement = error.result().itineraryResults().getFirst().legResults().getFirst();
    assertThat(requirement.candidates()).hasSize(1);
    LegMatchResult partialMatch = requirement.candidates().getFirst();
    assertThat(partialMatch.matchingCriteria()).containsExactly("route '[E]'");
    assertThat(partialMatch.missingCriteria()).containsExactly(expectedFareCriterion);
  }

  @Test
  void missingExpectedLegCannotPassWithAnotherSuccessfulLeg() {
    var plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of())));
    assertThrows(
        ItineraryAssertionError.class,
        () ->
            new ItineraryExpectation()
                .withLeg(new TransitLegExpectation().withRouteShortName("10"))
                .withLeg(new TransitLegExpectation().withRouteShortName("missing"))
                .evaluate(plan)
                .assertMatches());
  }

  @Test
  void unorderedMatchingFindsDistinctAssignmentForOverlappingExpectations() {
    var plan =
        tripPlan(
            itinerary(
                transitLeg("10", "Route 10", LegMode.BUS, Duration.ofMinutes(20), List.of()),
                transitLeg("20", "Route 20", LegMode.BUS, Duration.ofMinutes(20), List.of())));
    var result =
        new ItineraryExpectation()
            .withLeg(new TransitLegExpectation().withMode("BUS"))
            .withLeg(new TransitLegExpectation().withRouteShortName("10"))
            .evaluate(plan);
    result.assertMatches();
    var requirements = result.itineraryResults().getFirst().legResults();
    assertThat(requirements.get(0).matchedLeg().orElseThrow().actualLegIndex()).isEqualTo(1);
    assertThat(requirements.get(0).candidates().getFirst().actualLegIndex()).isEqualTo(0);
    assertThat(requirements.get(1).matchedLeg().orElseThrow().actualLegIndex()).isEqualTo(0);
    assertThat(MatchResultFormatter.format(result))
        .contains("Requirement 1: matched transit leg 2");
  }

  @ParameterizedTest
  @CsvSource({"ORDERED_SUBSEQUENCE", "UNORDERED_SUBSET"})
  void successfulSubsetRequirementsRetainFullPartialAndNonmatchingCandidates(
      TransitLegMatching mode) {
    var plan =
        tripPlan(
            itinerary(
                transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(10), List.of()),
                transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(15), List.of()),
                transitLeg("10", "10", LegMode.TRAM, Duration.ofMinutes(10), List.of()),
                transitLeg("30", "30", LegMode.TRAM, Duration.ofMinutes(10), List.of())));
    var result =
        new ItineraryExpectation()
            .withTransitLegMatching(mode)
            .withLeg(new TransitLegExpectation().withRouteShortName("10").withMode("BUS"))
            .evaluate(plan);

    assertThat(result.matches()).isTrue();
    var requirement = result.itineraryResults().getFirst().legResults().getFirst();
    var candidates = requirement.candidates();
    assertThat(candidates).hasSize(4);
    assertThat(requirement.matchedLeg()).hasValue(candidates.getFirst());
    assertThat(candidates.get(0).isFullMatch()).isTrue();
    assertThat(candidates.get(1).isFullMatch()).isTrue();
    assertThat(candidates.get(2).isPartialMatch()).isTrue();
    assertThat(candidates.get(2).matchingCriteria()).containsExactly("route '[10]'");
    assertThat(candidates.get(2).missingCriteria()).containsExactly("mode BUS");
    assertThat(candidates.get(3).isFullMatch()).isFalse();
    assertThat(candidates.get(3).isPartialMatch()).isFalse();
    String report = MatchResultFormatter.format(result);
    assertThat(
            report
                .lines()
                .filter(line -> line.stripLeading().startsWith("Candidate transit leg "))
                .count())
        .isEqualTo(4);
    assertThat(report)
        .contains(
            "Candidate transit leg 3: TRANSIT - Route: 10, From: From 10, To: To 10\n"
                + "        Matched: route '[10]'\n"
                + "        Missing: mode BUS");
  }

  @Test
  void requirementRejectsInconsistentAssignedMatches() {
    var plan =
        tripPlan(
            itinerary(
                transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(10), List.of()),
                transitLeg("10", "10", LegMode.TRAM, Duration.ofMinutes(10), List.of())));
    var requirement =
        new ItineraryExpectation()
            .withLeg(new TransitLegExpectation().withRouteShortName("10").withMode("BUS"))
            .evaluate(plan)
            .itineraryResults()
            .getFirst()
            .legResults()
            .getFirst();
    var assigned = requirement.matchedLeg();
    var partial = requirement.candidates().get(1);
    var criteria = requirement.expectedCriteria();
    var candidates = requirement.candidates();

    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LegRequirementResult(
                0, criteria, LegRequirementResult.Status.MATCHED, Optional.empty(), candidates));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LegRequirementResult(
                0, criteria, LegRequirementResult.Status.CRITERIA_MISMATCH, assigned, candidates));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LegRequirementResult(
                0,
                criteria,
                LegRequirementResult.Status.MATCHED,
                Optional.of(partial),
                candidates));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LegRequirementResult(
                0, criteria, LegRequirementResult.Status.MATCHED, assigned, List.of(partial)));
  }

  @ParameterizedTest
  @CsvSource({
    "EXACT_ORDERED,10:20,true,MATCHED:MATCHED,1",
    "EXACT_ORDERED,20:10,false,CRITERIA_MISMATCH:CRITERIA_MISMATCH,1",
    "EXACT_ORDERED,10:30:20,false,MATCHED:CRITERIA_MISMATCH,1",
    "ORDERED_SUBSEQUENCE,30:10:30:20:30,true,MATCHED:MATCHED,5",
    "ORDERED_SUBSEQUENCE,20:10,false,MATCHED:OUT_OF_ORDER,2",
    "UNORDERED_SUBSET,20:30:10,true,MATCHED:MATCHED,3",
    "UNORDERED_SUBSET,30,false,CRITERIA_MISMATCH:CRITERIA_MISMATCH,1"
  })
  void matchingModesHaveExplicitOrderAndCountSemantics(
      TransitLegMatching mode,
      String routes,
      boolean matches,
      String statuses,
      int candidatesPerRequirement) {
    var actual =
        java.util.Arrays.stream(routes.split(":"))
            .map(route -> transitLeg(route, route, LegMode.BUS, Duration.ofMinutes(10), List.of()))
            .toArray(Leg[]::new);
    var result =
        new ItineraryExpectation()
            .withTransitLegMatching(mode)
            .withLeg(new TransitLegExpectation().withRouteShortName("10"))
            .withLeg(new TransitLegExpectation().withRouteShortName("20"))
            .evaluate(tripPlan(itinerary(actual)));
    assertThat(result.matches()).isEqualTo(matches);
    assertThat(
            result.itineraryResults().getFirst().legResults().stream()
                .map(r -> r.status().name())
                .toList())
        .containsExactlyElementsIn(statuses.split(":"))
        .inOrder();
    result
        .itineraryResults()
        .getFirst()
        .legResults()
        .forEach(r -> assertThat(r.candidates()).hasSize(candidatesPerRequirement));
  }

  @ParameterizedTest
  @CsvSource({
    "EXACT_ORDERED,MISSING_LEG",
    "ORDERED_SUBSEQUENCE,ALREADY_ASSIGNED",
    "UNORDERED_SUBSET,ALREADY_ASSIGNED"
  })
  void aSingleActualLegCannotSatisfyTwoExpectedLegs(
      TransitLegMatching mode, LegRequirementResult.Status secondStatus) {
    var plan =
        tripPlan(itinerary(transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(10), List.of())));
    var result =
        new ItineraryExpectation()
            .withTransitLegMatching(mode)
            .withLeg(new TransitLegExpectation().withRouteShortName("10"))
            .withLeg(new TransitLegExpectation().withRouteShortName("10"))
            .evaluate(plan);
    assertThat(result.matches()).isFalse();
    var requirements = result.itineraryResults().getFirst().legResults();
    assertThat(requirements.get(0).status()).isEqualTo(LegRequirementResult.Status.MATCHED);
    assertThat(requirements.get(0).matchedLeg().orElseThrow().actualLegIndex()).isEqualTo(0);
    assertThat(requirements.get(1).status()).isEqualTo(secondStatus);
    assertThat(requirements.get(1).matchedLeg()).isEmpty();
  }

  @Test
  void explicitFalseProhibitsInterliningAndOmittedLeavesItUnconstrained() {
    var leg =
        transitLeg(
            "10",
            "10",
            LegMode.BUS,
            Duration.ofMinutes(10),
            List.of(),
            place("A"),
            place("B"),
            true);
    var plan = tripPlan(itinerary(leg));
    assertThat(
            new ItineraryExpectation()
                .withLeg(new TransitLegExpectation().withRouteShortName("10"))
                .evaluate(plan)
                .matches())
        .isTrue();
    var result =
        new ItineraryExpectation()
            .withLeg(
                new TransitLegExpectation()
                    .withRouteShortName("10")
                    .withInterlinedWithPreviousLeg(false))
            .evaluate(plan);
    assertThat(result.matches()).isFalse();
    assertThat(MatchResultFormatter.format(result))
        .contains("interlined with previous leg = false");
  }

  @Test
  void itineraryAndLegConditionsMustHoldOnTheSameItinerary() {
    var slow10 = itinerary(transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(40), List.of()));
    var fast20 = itinerary(transitLeg("20", "20", LegMode.BUS, Duration.ofMinutes(10), List.of()));
    var expected =
        new ItineraryExpectation()
            .withMaxDuration(Duration.ofMinutes(20))
            .withLeg(
                new TransitLegExpectation()
                    .withRouteShortName("10")
                    .withMaxDuration(Duration.ofMinutes(45)));
    assertThat(expected.evaluate(tripPlan(slow10, fast20)).matches()).isFalse();
    var fast10 = itinerary(transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(10), List.of()));
    var result = expected.evaluate(tripPlan(slow10, fast10));
    assertThat(result.matches()).isTrue();
    assertThat(result.itineraryResults()).hasSize(2);
    assertThat(result.itineraryResults().get(0).isSuccess()).isFalse();
    assertThat(result.itineraryResults().get(1).isSuccess()).isTrue();
  }

  @Test
  void itineraryDurationIncludesWaitingBetweenLegs() {
    var first = transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(10), List.of());
    var last = walkLeg(Duration.ofMinutes(5));
    var later =
        new Leg(
            last.from(),
            last.to(),
            START.plusMinutes(40),
            START.plusMinutes(45),
            last.realTime(),
            last.interlineWithPreviousLeg(),
            last.mode(),
            last.duration(),
            last.distance(),
            last.headsign(),
            last.route(),
            last.trip(),
            last.fareProducts(),
            last.accessibilityScore(),
            last.agency(),
            last.geometry(),
            last.rentedVehicle(),
            last.intermediatePlaces());
    assertThat(
            new ItineraryExpectation()
                .withMaxDuration(Duration.ofMinutes(30))
                .evaluate(tripPlan(itinerary(first, later)))
                .matches())
        .isFalse();
  }

  @Test
  void alternativesRetainDiagnosticsAndOnlyAssertionThrows() {
    var plan =
        tripPlan(itinerary(transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(10), List.of())));
    var route20 =
        new ItineraryExpectation().withLeg(new TransitLegExpectation().withRouteShortName("20"));
    var route30 =
        new ItineraryExpectation().withLeg(new TransitLegExpectation().withRouteShortName("30"));
    var failure = PlanExpectation.anyOf(route20, route30).evaluate(plan);
    assertThat(failure.matches()).isFalse();
    assertThat(failure.children()).hasSize(2);
    var error = assertThrows(ItineraryAssertionError.class, failure::assertMatches);
    assertThat(error.result()).isSameInstanceAs(failure);
    assertThat(error.getMessage()).contains("route '[20]'");
    assertThat(error.getMessage()).contains("route '[30]'");
    var success = PlanExpectation.anyOf(route20, new ItineraryExpectation()).evaluate(plan);
    assertThat(success.matches()).isTrue();
    assertThat(success.children()).hasSize(2);
    assertThat(PlanExpectation.allOf(route20, new ItineraryExpectation()).evaluate(plan).matches())
        .isFalse();
  }

  @Test
  void reportHasSingleSpacedLegsAndIndentedExpectationTree() {
    var plan =
        tripPlan(
            itinerary(
                walkLeg(Duration.ofMinutes(5)),
                transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(10), List.of())));
    var result =
        PlanExpectation.anyOf(
                PlanExpectation.allOf(
                    new ItineraryExpectation()
                        .withLeg(
                            new TransitLegExpectation().withRouteShortName("10").withMode("BUS"))))
            .evaluate(plan);

    assertThat(MatchResultFormatter.format(result))
        .isEqualTo(
            """
            Any expectation must match [matched]
              All expectations must match [matched]
                At least one itinerary must match [matched] (UNORDERED_SUBSET)
                  Leg 1: route '[10]', mode BUS
                  Itinerary 1 matched
                    Requirement 1: matched transit leg 1
                      Criteria: route '[10]', mode BUS
                      Candidate transit leg 1: TRANSIT - Route: 10, From: From 10, To: To 10
                        Matched: route '[10]', mode BUS
                    Actual leg 1: WALK - From: Walk From, To: Walk To, Distance: 1000m
                    Actual leg 2: TRANSIT - Route: 10, From: From 10, To: To 10""");
  }

  @Test
  void duplicateRequirementExplainsAssignmentConflictWithoutMissingCriteria() {
    var plan =
        tripPlan(itinerary(transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(10), List.of())));
    var result =
        new ItineraryExpectation()
            .withLeg(new TransitLegExpectation().withRouteShortName("10"))
            .withLeg(new TransitLegExpectation().withRouteShortName("10"))
            .evaluate(plan);

    var error = assertThrows(ItineraryAssertionError.class, result::assertMatches);
    assertThat(error.getMessage())
        .contains("Requirement 2: matching transit legs are already assigned");
    assertThat(error.getMessage()).contains("Candidate transit leg 1: TRANSIT - Route: 10");
    assertThat(error.getMessage()).doesNotContain("Missing:");
    assertThat(error.getMessage()).doesNotContain("missing:");
  }

  @Test
  void evaluationRetainsImmutableResultsAndOriginalItineraryAndCandidateContext() {
    var legs =
        new ArrayList<>(
            List.of(
                walkLeg(Duration.ofMinutes(5)),
                transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(10), List.of()),
                transitLeg("20", "20", LegMode.BUS, Duration.ofMinutes(10), List.of())));
    var itineraries =
        new ArrayList<>(List.of(itinerary(), new Itinerary(legs, OptionalDouble.empty())));
    var expectation =
        new ItineraryExpectation()
            .withLeg(new TransitLegExpectation().withRouteShortName("20").withMode("TRAM"));
    var result = expectation.evaluate(new TripPlan(itineraries, "next", "previous"));
    String report = MatchResultFormatter.format(result);

    legs.clear();
    itineraries.clear();
    expectation.withLeg(new TransitLegExpectation().withRouteShortName("30"));

    var itineraryResult = result.itineraryResults().get(1);
    var requirement = itineraryResult.legResults().getFirst();
    assertThat(requirement.matchedLeg()).isEmpty();
    assertThat(requirement.candidates()).hasSize(2);
    var candidate = requirement.candidates().get(1);
    assertThat(MatchResultFormatter.format(result)).isEqualTo(report);
    assertThat(result.plan().itineraries()).hasSize(2);
    assertThat(result.plan().itineraries().get(1).legs()).hasSize(3);
    assertThat(itineraryResult.itineraryIndex()).isEqualTo(1);
    assertThat(itineraryResult.itinerary().legs()).hasSize(3);
    assertThat(requirement.expectedLegIndex()).isEqualTo(0);
    assertThat(requirement.expectedCriteria())
        .containsExactly("route '[20]'", "mode TRAM")
        .inOrder();
    assertThat(candidate.actualLegIndex()).isEqualTo(1);
    assertThat(candidate.leg().route().getShortName()).isEqualTo("20");
    assertThat(requirement.candidates().getFirst().matchingCriteria()).isEmpty();
    assertThat(requirement.candidates().getFirst().missingCriteria())
        .containsExactly("route '[20]'", "mode TRAM")
        .inOrder();
    assertThrows(UnsupportedOperationException.class, () -> candidate.matchingCriteria().clear());
    assertThrows(UnsupportedOperationException.class, () -> candidate.missingCriteria().clear());
    assertThrows(UnsupportedOperationException.class, () -> requirement.candidates().clear());
    assertThrows(UnsupportedOperationException.class, () -> itineraryResult.legResults().clear());
    assertThrows(
        UnsupportedOperationException.class, () -> itineraryResult.itinerary().legs().clear());
    assertThrows(UnsupportedOperationException.class, () -> result.plan().itineraries().clear());
  }

  @ParameterizedTest
  @CsvSource({
    "10,10,10,20,true,true,false",
    "20,10,10,10,true,false,false",
    "10,10,20,10,true,false,false",
    "10,30,10,10,true,false,false",
    "10,10,10,30,true,false,false",
    "20,30,20,30,false,false,true",
    "10,30,20,10,false,false,true"
  })
  void itineraryMatchingAppliesToLegAndItineraryConditions(
      String firstRoute,
      int firstMinutes,
      String secondRoute,
      int secondMinutes,
      boolean anyMatches,
      boolean allMatch,
      boolean noneMatch) {
    var plan =
        tripPlan(
            itinerary(
                transitLeg(
                    firstRoute,
                    firstRoute,
                    LegMode.BUS,
                    Duration.ofMinutes(firstMinutes),
                    List.of())),
            itinerary(
                transitLeg(
                    secondRoute,
                    secondRoute,
                    LegMode.BUS,
                    Duration.ofMinutes(secondMinutes),
                    List.of())));
    var expected =
        new ItineraryExpectation()
            .withMaxDuration(Duration.ofMinutes(20))
            .withLeg(new TransitLegExpectation().withRouteShortName("10"));

    assertThat(expected.withItineraryMatching(ItineraryMatching.ANY).evaluate(plan).matches())
        .isEqualTo(anyMatches);
    assertThat(expected.withItineraryMatching(ItineraryMatching.ALL).evaluate(plan).matches())
        .isEqualTo(allMatch);
    assertThat(expected.withItineraryMatching(ItineraryMatching.NONE).evaluate(plan).matches())
        .isEqualTo(noneMatch);
  }

  @ParameterizedTest
  @CsvSource({"ALL,Every itinerary must match", "NONE,No itinerary must match"})
  void itineraryMatchingRetainsDiagnosticsForSuccessfulAndFailedItineraries(
      ItineraryMatching mode, String description) {
    var plan =
        tripPlan(
            itinerary(transitLeg("10", "10", LegMode.BUS, Duration.ofMinutes(10), List.of())),
            itinerary(transitLeg("20", "20", LegMode.BUS, Duration.ofMinutes(30), List.of())));
    var expected =
        new ItineraryExpectation()
            .withItineraryMatching(mode)
            .withTransitLegMatching(TransitLegMatching.EXACT_ORDERED)
            .withMaxDuration(Duration.ofMinutes(20))
            .withLeg(new TransitLegExpectation().withRouteShortName("10"));

    var result = expected.evaluate(plan);
    var error = assertThrows(ItineraryAssertionError.class, result::assertMatches);

    assertThat(error.result()).isSameInstanceAs(result);
    assertThat(result.matchingMode()).isEqualTo(TransitLegMatching.EXACT_ORDERED);
    assertThat(result.expectedLegs()).containsExactly(List.of("route '[10]'"));
    assertThat(result.itineraryCriteria()).containsExactly("Maximum itinerary duration: PT20M");
    assertThat(result.itineraryResults()).hasSize(2);
    assertThat(result.itineraryResults().get(0).isSuccess()).isTrue();
    assertThat(result.itineraryResults().get(1).isSuccess()).isFalse();
    assertThat(error.getMessage()).contains(description + " [failed]");
    assertThat(error.getMessage()).contains("Itinerary 1 matched");
    assertThat(error.getMessage()).contains("Itinerary 2 failed");
    assertThat(error.getMessage()).contains("route '[10]'");
    assertThat(error.getMessage()).contains("Itinerary duration PT30M exceeds PT20M");
  }

  @Test
  void absentAndEmptyResultsHaveDistinctSemantics() {
    var empty = tripPlan();
    assertThat(new ItineraryExpectation().evaluate(empty).matches()).isFalse();
    assertThat(
            new ItineraryExpectation()
                .withItineraryMatching(ItineraryMatching.ALL)
                .evaluate(empty)
                .matches())
        .isFalse();
    assertThat(
            new ItineraryExpectation()
                .withItineraryMatching(ItineraryMatching.NONE)
                .evaluate(empty)
                .matches())
        .isTrue();
    assertThat(PlanExpectation.itineraryCountBetween(0, 0).evaluate(empty).matches()).isTrue();
    var walking = tripPlan(itinerary(walkLeg(Duration.ofMinutes(10))));
    var noTransit =
        new ItineraryExpectation().withTransitLegMatching(TransitLegMatching.EXACT_ORDERED);
    assertThat(noTransit.evaluate(walking).matches()).isTrue();
    assertThat(noTransit.withItineraryMatching(ItineraryMatching.NONE).evaluate(walking).matches())
        .isFalse();
    assertThat(PlanExpectation.itineraryCountBetween(0, 0).evaluate(walking).matches()).isFalse();
  }

  private static TripPlan tripPlan(Itinerary... itineraries) {
    return new TripPlan(List.of(itineraries), "", "");
  }

  private static Itinerary itinerary(Leg... legs) {
    return new Itinerary(List.of(legs), OptionalDouble.empty());
  }

  private static Leg walkLeg(Duration duration) {
    return new Leg(
        place("Walk From"),
        place("Walk To"),
        START,
        START.plus(duration),
        false,
        false,
        LegMode.WALK,
        duration,
        1000,
        Optional.empty(),
        null,
        null,
        List.of(),
        OptionalDouble.empty(),
        null,
        new LegGeometry(GEOMETRY),
        false,
        Optional.empty());
  }

  private static Leg transitLeg(
      String routeShortName,
      String routeLongName,
      LegMode mode,
      Duration duration,
      List<FareProductUse> fareProducts) {
    String idToken = routeShortName != null ? routeShortName : routeLongName;
    return transitLeg(
        routeShortName,
        routeLongName,
        mode,
        duration,
        fareProducts,
        place("From " + idToken),
        place("To " + idToken),
        false);
  }

  private static Leg transitLeg(
      String routeShortName,
      String routeLongName,
      LegMode mode,
      Duration duration,
      List<FareProductUse> fareProducts,
      Place from,
      Place to,
      boolean interlineWithPreviousLeg) {
    String idToken = routeShortName != null ? routeShortName : routeLongName;
    Route route =
        Route.builder()
            .setId("route-" + idToken)
            .setGtfsId("gtfs-" + idToken)
            .setShortName(routeShortName)
            .setLongName(routeLongName)
            .build();

    return new Leg(
        from,
        to,
        START,
        START.plus(duration),
        false,
        interlineWithPreviousLeg,
        mode,
        duration,
        1000,
        Optional.empty(),
        route,
        new Trip("trip-" + idToken, Optional.empty(), Optional.empty()),
        fareProducts,
        OptionalDouble.empty(),
        new Agency("agency", "Test Agency"),
        new LegGeometry(GEOMETRY),
        false,
        Optional.empty());
  }

  private static Place place(String name) {
    return new Place(
        name, 10.0f, 10.0f, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
  }

  private static Place place(String name, String stopGtfsId, String stopCode) {
    Stop stop =
        new Stop(
            name,
            stopGtfsId,
            Optional.ofNullable(stopCode),
            Optional.empty(),
            Optional.empty(),
            null);
    return new Place(
        name,
        10.0f,
        10.0f,
        Optional.of(stop),
        Optional.empty(),
        Optional.empty(),
        Optional.empty());
  }

  private static FareProductUse fare(String riderCategoryId, String mediumId) {
    return new FareProductUse(
        "fare-" + riderCategoryId + "-" + mediumId,
        new FareProductUse.FareProduct(
            "product-" + riderCategoryId + "-" + mediumId,
            "Test fare",
            null,
            Optional.of(new FareProductUse.FareProduct.RiderCategory(riderCategoryId, "Rider")),
            Optional.of(new FareProductUse.FareProduct.FareMedium(mediumId, "Medium"))));
  }

  private static FareProductUse fare(Float amount, String riderCategoryId, String mediumId) {
    return new FareProductUse(
        "fare-" + riderCategoryId + "-" + mediumId,
        new FareProductUse.FareProduct(
            "product-" + riderCategoryId + "-" + mediumId,
            "Test fare",
            new Money(BigDecimal.valueOf(amount), new Currency(2, "USD")),
            Optional.of(new FareProductUse.FareProduct.RiderCategory(riderCategoryId, "Rider")),
            Optional.of(new FareProductUse.FareProduct.FareMedium(mediumId, "Medium"))));
  }
}
