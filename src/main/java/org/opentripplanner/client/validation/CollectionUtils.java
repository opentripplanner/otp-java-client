package org.opentripplanner.client.validation;

import java.util.Collection;
import java.util.Objects;

public class CollectionUtils {
  public static <T> Collection<T> assertHasValue(Collection<T> coll) {
    Objects.requireNonNull(coll);
    if (coll.isEmpty()) {
      throw new IllegalArgumentException("Collection has no elements.");
    }
    return coll;
  }
}
