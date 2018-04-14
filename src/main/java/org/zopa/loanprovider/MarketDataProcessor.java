package org.zopa.loanprovider;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by mdagostino on 4/13/18.
 * Currently only provide a single datasource (file).
 * The idea is enable get data from other sources.
 */
public class MarketDataProcessor {

    private final List<LenderData> marketData;

    public MarketDataProcessor(String filePath) {
        // TODO: ImmutableList.copyOf
        marketData = FileReader.getMarketData(filePath);
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
                .map(data -> data.getAvailable())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalAvailable.compareTo(amountRequested) >= 0;

    }

    private Loan calculateLoanFor(BigDecimal amountRequested, int months) {
        BigDecimal amountCollector = new BigDecimal(0);
        Map<LenderData, BigDecimal> lendersCollector = new HashMap<>();
        LenderData lenderData;
        BigDecimal available;
        BigDecimal borrowed;
        BigDecimal diff;

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


        BigDecimal loanRate = calculateShareRate(lendersCollector, amountRequested);
        BigDecimal monthtlyPayment = calculateMonthlyPayment(loanRate, amountRequested);
        BigDecimal totalRepayment = monthtlyPayment.multiply(BigDecimal.valueOf(months));
        return new Loan(amountRequested, loanRate, monthtlyPayment, totalRepayment);
    }

    private BigDecimal calculateShareRate(Map<LenderData, BigDecimal> lendersCollector, BigDecimal amountRequested) {
        return lendersCollector
                .entrySet()
                .stream()
                .map(data -> data.getValue().divide(amountRequested).multiply(data.getKey().getRate()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal rate, BigDecimal amount) {
        double periodicRate = rate.doubleValue() / 12;
        double pow = Math.pow(1 + periodicRate, 3 * 12);
        BigDecimal factor = new BigDecimal((pow * periodicRate) / (pow - 1));
        return amount.multiply(factor);
    }

}
