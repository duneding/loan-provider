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
    private static final int DEFAULT_MONTHS = 36;
    private static final int MIN_AMOUNT = 1000;
    private static final int MAX_AMOUNT = 15000;

    public static void main(String[] args) {

        if (args.length == 2) {

            final String marketFileParam = args[0];
            final BigDecimal loanAmountParam;

            try {
                loanAmountParam = new BigDecimal(args[1]);

                if (isValidAmount(loanAmountParam) && isBetweenMinMax(loanAmountParam)) {

                    // I/O blocking read. For other kind of app (not console) should be use async read
                    List<LenderData> marketData = FileReader.getMarketData(marketFileParam);
                    Optional<Loan> loan = getLoan(marketData, loanAmountParam);
                    Printer.printResult(loan);

                } else {
                    Printer.printError(format("Amount requested must be between {0} and {1}", String.valueOf(MIN_AMOUNT), String.valueOf(MAX_AMOUNT)));
                    Printer.printError("Amount requested must be for 100 increment");
                }

            } catch (NumberFormatException e) {
                Printer.printError("Amount parameter must be a number");
                throw new NumberFormatException();
            } catch (IOException e) {
                Printer.printError("Error loading market data");
            } catch (ArithmeticException e) {
                Printer.printError(e.getMessage());
            } catch (RuntimeException e) {
                Printer.printError(e.getMessage());
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
        return amount.remainder(BigDecimal.valueOf(100)).equals(BigDecimal.valueOf(0));
    }

    public static Optional<Loan> getLoan(List<LenderData> marketData, BigDecimal amount) throws IOException {
        AmortizationMethod amortizationMethod = new FrenchAmortizationMethod();
        LoanProcessor processor = new LoanProcessor(marketData, amortizationMethod);
        return processor.findLoanFor(amount, DEFAULT_MONTHS);
    }

}
