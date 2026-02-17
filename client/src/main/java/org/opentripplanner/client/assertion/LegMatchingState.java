package org.opentripplanner.client.assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.opentripplanner.client.model.Leg;

/** Stores criterion match results for a leg. */
public class LegMatchingState {

  private final Leg leg;
  private final List<String> matchingCriteria = new ArrayList<>();
  private final List<String> missingCriteria = new ArrayList<>();

  public LegMatchingState(Leg leg) {
    this.leg = leg;
  }

  public Leg getLeg() {
    return leg;
  }

  public void addMatch(String criterion) {
    matchingCriteria.add(criterion);
  }

  public void addFailure(String criterion) {
    missingCriteria.add(criterion);
  }

  public boolean isFullMatch() {
    return missingCriteria.isEmpty() && !matchingCriteria.isEmpty();
  }

  public boolean hasAnyMatch() {
    return !matchingCriteria.isEmpty();
  }

  public String getMatchingCriteria() {
    return String.join(", ", matchingCriteria);
  }

  public String getMissingCriteria() {
    return String.join(", ", missingCriteria);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LegMatchingState that = (LegMatchingState) o;
    return (matchingCriteria.equals(that.matchingCriteria)
        && missingCriteria.equals(that.missingCriteria));
  }

  @Override
  public int hashCode() {
    return Objects.hash(matchingCriteria, missingCriteria);
  }
}
