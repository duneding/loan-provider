package org.zopa.loanprovider;

import java.math.BigDecimal;

/**
 * Constant quote.
 */
public class FrenchAmortizationMethod implements AmortizationMethod {

    /**
     * French method to amortizing loan (monthly payment fixed).
     *
     * @param rate
     * @param amount
     * @return
     */
    public BigDecimal calculateMonthlyPayment(BigDecimal rate, BigDecimal amount, int months) {
        BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(12), BigDecimal.ROUND_CEILING);
        BigDecimal pow = monthlyRate.add(BigDecimal.valueOf(1)).pow(months);
        BigDecimal factor = pow
                .multiply(monthlyRate)
                .divide(pow.subtract(BigDecimal.valueOf(1)), BigDecimal.ROUND_CEILING);
        return amount.multiply(factor);
    }

}
