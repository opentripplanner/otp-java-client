package org.opentripplanner.assertions;

import java.util.List;
import java.util.function.Predicate;
import org.opentripplanner.client.model.Leg;

/**
 * Used to store one criterion to be applied to a leg.
 *
 * @param message Message describing this criterion
 * @param test A consumer that applies checks and updates the matching state
 */
public record LegCriterion(String message, Predicate<Leg> test) {
    public static String describeCriteria(List<LegCriterion> criterionList) {
        StringBuilder message = new StringBuilder();
        for (LegCriterion criterion : criterionList) {
            message.append(criterion.message()).append("\n");
        }
        return message.toString();
    }
}
