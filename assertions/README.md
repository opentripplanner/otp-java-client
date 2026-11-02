# OTP Java Assertions
The Assertions package is a utility for validating quality of results from OTP instances, designed to be used with the Java client's response objects. The assertions are defined through builder APIs that let you specify the itinerary and leg conditions, then assert that the trip plan contains the expected pieces. 

```java
var expected = new ItineraryExpectation()
        .withTransitLegMatching(TransitLegMatching.EXACT_ORDERED)
        .withMaxDuration(Duration.ofHours(1))
        .withLeg(new TransitLegExpectation()
                .withMode("BUS")
                .withRouteShortName("2")
                .withInterlinedWithPreviousLeg(false));

PlanMatchResult result = expected.evaluate(plan);
String report = MatchResultFormatter.format(result);
// Inspect or report both successful and failed results before failing the test.
result.assertMatches();
```

By default, at least one itinerary must match. To require every itinerary to
satisfy the same expectation, use:

```java
expected.withItineraryMatching(ItineraryMatching.ALL).evaluate(plan).assertMatches();
```

This fails for an empty plan and retains diagnostics for every itinerary.
`PlanExpectation.allOf(...)` instead requires every supplied expectation to match;
each expectation can match a different itinerary.

## API Documentation
The API for building expectations is a series of builder patterns used as building blocks to allow for flexibility in defining the exact conditions you'd like to check for in the OTP plan response. 

### PlanExpectation interface
PlanExpectation is just a functional interface that exposes an `evaluate` method, used to validate the expectation against an OTP Client `TripPlan`. Most PlanExpectation instances you will see in the wild are probably going to be `ItineraryExpectation`s.

It contains a factory function that can be used to create a PlanExpectation to require a certain number of itineraries on a TripPlan with `itineraryCountBetween`.

#### Composing expectations
Use `PlanExpectation.allOf(...)` and `PlanExpectation.anyOf(...)` to compose
expectations. These accept as many PlanExpectations as you want and return one that combines all of them, either requiring all to match or at least one. 

### ItineraryExpectation: Define the expectations for an itinerary
Create a new expectation with `new ItinteraryExpectation()`. The ItineraryExpectation uses a builder pattern to set up the expectations. Although you can specify some conditions on the itinerary itself, such as the max duration, the main purpose of the ItineraryExpectation is to contain leg expectations, validating that the itinerary contains the expected legs. 

Each leg expectation can be added with `withLeg(transitLegExpectation)`, which accepts a TransitLegExpectation, containing the set of criteria that is expected to be true for that leg. 

You can customize how exact the leg expectations need to be matched against the actual itinerary. 

#### TransitLegMatching
- **EXACT_ORDERED**: The strictest setting, this ensures that each leg exists in the itinerary, in order, with no extra transit legs (walking, biking, etc legs are okay).
- **ORDERED_SUBSEQUENCE**: Compared to EXACT_ORDERED, this removes the restriction on extra legs. It only requires that the expected legs exist in order.
- **UNORDERED_SUBSET**: The loosest setting, it simply requires that each expected leg exists in the actual itinerary. It uses a maximum matching algorithm to ensure all legs are matched with an expectation, if one is available. 

#### Evaluate against a real TripPlan
Evaluate a real life OTP TripPlan object (returned by the OTP Client library) against your shiny new `ItineraryExpectation` by calling the `.evaluate(TripPlan)` method. It will return a PlanMatchResult which you can use to dig into the successes and failures. 

Use `withItineraryMatching(ItineraryMatching)` to choose how many itineraries must satisfy all configured leg and itinerary conditions:

- **ANY** (default): At least one itinerary must match. Fails for an empty plan.
- **ALL**: Every itinerary must match, and the plan must contain at least one itinerary.
- **NONE**: No itinerary may match. Succeeds for an empty plan.

Every mode retains diagnostics for all itineraries directly in the result's `itineraryResults()`.

#### Composing ItineraryExpectations 
ItineraryExpectations can be composed as like any PlanExpectation. See the [PlanExpectation Composition](asdf) section for more details.

### TransitLegExpectations: Define the expectations for a leg
This class is used to set up your expectations for a transit leg. Create a new instance and then define your criteria by using the builder methods.

### Inspecting Results
Calling `evaluate` returns a PlanMatchResult, which provides a snapshot of the results of evaluating the expectation against a trip plan. Inside this object you can find all the details of the expected legs, the matched actual legs, partial matches, etc. from the itinerary matching. For structured data reporting, you can dig into the results like this:

```java
for (ItineraryMatchResult itinerary : result.itineraryResults()) {
    for (LegRequirementResult requirement : itinerary.legResults()) {
        var status = requirement.status();
        Optional<LegMatchResult> assigned = requirement.matchedLeg();
        var partialMatches = requirement.candidates().stream()
                .filter(LegMatchResult::isPartialMatch)
                .toList();
        for (LegMatchResult candidate : requirement.candidates()) {
            List<String> satisfied = candidate.matchingCriteria();
            List<String> failed = candidate.missingCriteria();
        }
    }
}
```

The result hierarchy is `PlanMatchResult` > `ItineraryMatchResult` >
`LegRequirementResult` > `LegMatchResult`.
Use `PlanMatchResult.matches()` to check whether the entire expectation succeeded.
Its `itineraryResults()` contains the evaluated itineraries and their outcomes.

Each itinerary's `legResults()` contains one result per expected leg, with its
criteria, status, the assigned match (if it exists), and the results of matching the criteria against all the legs (candidates). You can inspect each candidate with isFullMatch and isPartialMatch. 

Use `MatchResultFormatter.format(result)` to turn the structured results into
text for logging or display:

```java
String report = MatchResultFormatter.format(result);
System.out.println(report);
```

For the expectation at the top of this page, suppose the plan contains two
direct bus itineraries from Downtown to Airport: route 3 taking 25 minutes and
route 2 taking 30 minutes. Neither leg is interlined. The formatter produces:

```text
At least one itinerary must match [matched] (EXACT_ORDERED)
  Leg 1: mode BUS, route '[2]', interlined with previous leg = false
  Maximum itinerary duration: PT1H
  Itinerary 1 failed
    Requirement 1: Transit leg at position 1 does not match criteria
      Criteria: mode BUS, route '[2]', interlined with previous leg = false
      Candidate transit leg 1: TRANSIT - Route: 3, From: Downtown, To: Airport
        Matched: mode BUS, interlined with previous leg = false
        Missing: route '[2]'
    Actual leg 1: TRANSIT - Route: 3, From: Downtown, To: Airport
  Itinerary 2 matched
    Requirement 1: matched transit leg 1
      Criteria: mode BUS, route '[2]', interlined with previous leg = false
      Candidate transit leg 1: TRANSIT - Route: 2, From: Downtown, To: Airport
        Matched: mode BUS, route '[2]', interlined with previous leg = false
    Actual leg 1: TRANSIT - Route: 2, From: Downtown, To: Airport
```

The first itinerary has a partial leg match: its mode and interlining satisfy
the criteria, but its route does not. The overall result matches because the
second itinerary satisfies every condition.
