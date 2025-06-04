package org.opentripplanner.client.model;

import java.math.BigDecimal;

public record Money(BigDecimal amount, Currency currency) {}
