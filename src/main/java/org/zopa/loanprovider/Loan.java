package org.zopa.loanprovider;

import java.math.BigDecimal;

/**
 * DTO to store all information required for a Loan offer.
 */
public class Loan {

    private final BigDecimal requestedAmount;
    private final BigDecimal rate;
    private final BigDecimal monthlyRepayment;
    private final BigDecimal totalRepayment;

    public Loan(BigDecimal requestedAmount, BigDecimal rate, BigDecimal monthlyRepayment, BigDecimal totalRepayment) {
        this.requestedAmount = requestedAmount;
        this.rate = rate;
        this.monthlyRepayment = monthlyRepayment;
        this.totalRepayment = totalRepayment;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getMonthlyRepayment() {
        return monthlyRepayment;
    }

    public BigDecimal getTotalRepayment() {
        return totalRepayment;
    }
}
