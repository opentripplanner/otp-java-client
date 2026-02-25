package org.opentripplanner.assertions;

import java.util.function.Consumer;

/**
 * Used to store one criterion to be applied to a leg.
 *
 * @param message Message describing this criterion
 * @param test A consumer that applies checks and updates the matching state
 */
public record LegCriterion(String message, Consumer<LegMatchingState> test) {}
