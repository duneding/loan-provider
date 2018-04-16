package org.zopa.loanprovider;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Currently only provide a single datasource (file).
 * The idea is enable get data from other sources.
 */
public class LoanProcessor {

    private final List<LenderData> marketData;
    private final AmortizationMethod amortizationMethod;

    public LoanProcessor(List<LenderData> marketData, AmortizationMethod amortizationMethod) {
        this.marketData = marketData;
        this.amortizationMethod = amortizationMethod;
    }

    public Optional<Loan> findLoanFor(BigDecimal amount, int months) {
        if (sufficientOffers(marketData, amount)) {
            // Loan is possible. So calculate best match
            marketData.sort(LoanComparator.byRate);
            return Optional.of(calculateLoanFor(amount, months));
        } else {
            return Optional.empty();
        }
    }

    private boolean sufficientOffers(List<LenderData> marketData, BigDecimal amountRequested) {
        BigDecimal totalAvailable = marketData.stream()
                .map(LenderData::getAvailable)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAvailable.compareTo(amountRequested) >= 0;

    }

    /**
     * Select lenders, calculate rate and build the loan result.
     *
     * @param amountRequested
     * @param months
     * @return
     */
    private Loan calculateLoanFor(BigDecimal amountRequested, int months) {
        Map<LenderData, BigDecimal> bestLenders = collectLendersFor(amountRequested);
        BigDecimal loanRate = calculateShareRate(bestLenders, amountRequested);
        BigDecimal monthtlyPayment = amortizationMethod.calculateMonthlyPayment(loanRate, amountRequested, months);
        BigDecimal totalRepayment = monthtlyPayment.multiply(BigDecimal.valueOf(months));
        return new Loan(amountRequested, loanRate, monthtlyPayment, totalRepayment);
    }

    /**
     * Collect all lenders until get the amount requested.
     * TODO: substract amount given for each lender.
     * Currently it is not updating available money. It assumes other external process should do it.
     *
     * @param amountRequested
     * @return
     */
    private Map<LenderData, BigDecimal> collectLendersFor(BigDecimal amountRequested) {
        BigDecimal amountCollector = new BigDecimal(0);
        Map<LenderData, BigDecimal> lendersCollector = new HashMap<>();
        LenderData lenderData;
        BigDecimal available;
        BigDecimal borrowed;
        BigDecimal diff;

        // Iterate over the lenders until reach the total amount.
        for (int i = 0; i < marketData.size() && amountCollector.compareTo(amountRequested) < 0; i++) {
            lenderData = marketData.get(i);
            available = lenderData.getAvailable();

            if (amountCollector.add(available).compareTo(amountRequested) == 1) {
                diff = amountCollector
                        .add(available)
                        .subtract(amountRequested);
                borrowed = available.subtract(diff);
                amountCollector = amountCollector.add(borrowed);
            } else {
                borrowed = available;
                amountCollector = amountCollector.add(available);
            }
            lendersCollector.put(lenderData, borrowed);
        }

        return lendersCollector;
    }

    /**
     * Money-Weighted Rate calculator.
     *
     * @param lendersCollector
     * @param amountRequested
     * @return
     */
    private BigDecimal calculateShareRate(Map<LenderData, BigDecimal> lendersCollector, BigDecimal amountRequested) {
        try {
            return lendersCollector
                    .entrySet()
                    .stream()
                    .map(data -> data.getValue()
                            .divide(amountRequested, 4, BigDecimal.ROUND_CEILING)
                            .multiply(data.getKey().getRate()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (ArithmeticException e) {
            throw new ArithmeticException("Error calculating money-weighted rate");
        }
    }

}
