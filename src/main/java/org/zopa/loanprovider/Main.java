package org.zopa.loanprovider;

import java.io.IOException;
import java.math.BigDecimal;
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

    public static void main(String[] args) {

        if (args.length == 2) {

            final String marketFileParam = args[0];
            final BigDecimal loanAmountParam;
            try {
                loanAmountParam = new BigDecimal(args[1]);
            } catch (NumberFormatException e) {
                Printer.printError("Amount parameter must be a number");
                throw new NumberFormatException();
            }

            if (isValidAmount(loanAmountParam) && isBetweenMinMax(loanAmountParam)) {
                try {
                    Optional<Loan> loan = getLoan(marketFileParam, loanAmountParam);
                    Printer.printResult(loan);
                } catch (IOException e) {
                    //It should use a logging library (ej. log4j)
                    Printer.printError("Error loading market data");
                } catch (RuntimeException e) {
                    Printer.printError("Error reading market data file");
                }
            } else {
                Printer.printError(format("Amount requested must be between {0} and {1}", String.valueOf(MIN_AMOUNT), String.valueOf(MAX_AMOUNT)));
                Printer.printError("Amount requested must be for 100 increment");
            }

        } else {
            Printer.printError("Parameters provided must be 2: [market file] [loan amount]");
        }

    }

    private static boolean isBetweenMinMax(BigDecimal amount) {
        return amount.compareTo(BigDecimal.valueOf(MIN_AMOUNT)) >= 0 &&
                amount.compareTo(BigDecimal.valueOf(MAX_AMOUNT)) <= 0;
    }

    private static boolean isValidAmount(BigDecimal amount) {
        return amount.remainder(BigDecimal.valueOf(100)) == BigDecimal.valueOf(0);
    }

    public static Optional<Loan> getLoan(String marketFile, BigDecimal amount) throws IOException {
        List<LenderData> marketData = FileReader.getMarketData(marketFile);
        MarketDataProcessor processor = new MarketDataProcessor(marketData);
        return processor.findLoanFor(amount, DEFAULT_MONTHS);
    }

}
