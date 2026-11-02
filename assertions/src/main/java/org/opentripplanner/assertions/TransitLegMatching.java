package org.opentripplanner.assertions;

/** All modes match distinct transit legs and ignore non-transit legs. */
public enum TransitLegMatching {
  /** Identical transit-leg count and order. */
  EXACT_ORDERED,
  /** Expected legs occur in order, with additional transit legs allowed anywhere. */
  ORDERED_SUBSEQUENCE,
  /** Expected legs occur in any order, with additional transit legs allowed. */
  UNORDERED_SUBSET
}
