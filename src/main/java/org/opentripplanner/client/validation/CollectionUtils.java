package org.opentripplanner.client.validation;

import java.util.Collection;
import java.util.Objects;

public class CollectionUtils {
  public static void assertHasValue(Collection<?> coll) {
    Objects.requireNonNull(coll);
    if (coll.isEmpty()) {
      throw new IllegalArgumentException("Collection has no elements.");
    }
  }
}
