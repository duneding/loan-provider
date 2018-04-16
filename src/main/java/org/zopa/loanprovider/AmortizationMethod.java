package org.zopa.loanprovider;

import java.math.BigDecimal;

/**
 * Abstract amortization.
 * Currently only used for french method.
 */
public interface AmortizationMethod {

    BigDecimal calculateMonthlyPayment(BigDecimal rate, BigDecimal amount, int months);
}
