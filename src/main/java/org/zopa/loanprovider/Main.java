package org.zopa.loanprovider;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

import static java.text.MessageFormat.format;

/**
 * Main class to execute the app:
 * Parameters to run:
 * [market data file] + [amount_requested]
 */
public class Main {

    //This should be config from file or other source to be more flexible
    private static int DEFAULT_MONTHS = 36;
    private static int MIN_AMOUNT = 1000;
    private static int MAX_AMOUNT = 15000;

    public static void main(String[] args) throws IOException {

        if (args.length == 2) {

            final String marketFileParam = args[0];
            final BigDecimal loanAmountParam = new BigDecimal(args[1]);

            boolean betweenBand = loanAmountParam.compareTo(BigDecimal.valueOf(MIN_AMOUNT)) >= 0 &&
                    loanAmountParam.compareTo(BigDecimal.valueOf(MAX_AMOUNT)) <= 0;

            if (betweenBand) {
                try {
                    Optional<Loan> loan = getLoan(marketFileParam, loanAmountParam);
                    if (loan.isPresent()) {
                        Printer.printLoan(loan.get());
                    } else {
                        Printer.printLoanNotPossible();
                    }
                } catch (IOException e) {
                    //It should use a logging library (ej. log4j)
                    Printer.printError("Error loading market data");
                }
            } else {
                Printer.printError(format("Amount requested must be between {0} and {1}", MIN_AMOUNT, MAX_AMOUNT));
            }

        } else {
            throw new InvalidParameterException("Parameters provided must be 2: [market file] [loan amount]");
        }

    }

    public static Optional<Loan> getLoan(String marketFile, BigDecimal amount) throws IOException {
        List<LenderData> marketData = FileReader.getMarketData(marketFile);
        MarketDataProcessor processor = new MarketDataProcessor(marketData);
        return processor.findLoanFor(amount, DEFAULT_MONTHS);
    }

}
