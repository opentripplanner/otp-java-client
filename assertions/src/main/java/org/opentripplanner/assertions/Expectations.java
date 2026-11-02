package org.opentripplanner.assertions;

/** Static factories for composing the existing itinerary and transit-leg builders. */
public final class Expectations {
  private Expectations() {}

  /** Requires a transit leg with one of the supplied route short names. */
  public static TransitLegExpectation route(String... names) {
    return new TransitLegExpectation().withRouteShortName(names);
  }

  /** Requires a transit leg with one of the supplied route long names. */
  public static TransitLegExpectation routeLongName(String... names) {
    return new TransitLegExpectation().withRouteLongName(names);
  }

  /**
   * Requires one itinerary containing all supplied transit legs, in any order, with additional
   * transit legs allowed. Non-transit legs are ignored.
   */
  public static ItineraryExpectation containingTransitLegs(TransitLegExpectation... legs) {
    var expectation = new ItineraryExpectation();
    for (var leg : legs) {
      expectation.withLeg(leg);
    }
    return expectation;
  }

  /**
   * Requires one itinerary with exactly the supplied transit legs, in declaration order.
   * Non-transit legs are ignored.
   */
  public static ItineraryExpectation exactTransitLegs(TransitLegExpectation... legs) {
    return containingTransitLegs(legs).withTransitLegMatching(TransitLegMatching.EXACT_ORDERED);
  }
}
