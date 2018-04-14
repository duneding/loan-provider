package org.zopa.loanprovider;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

import static java.text.MessageFormat.format;

/**
 * Created by mdagostino on 4/13/18.
 */
public class Main {

    //It should be config from file or other source to be more flexible
    private static int DEFAULT_MONTHS = 36;

    public static void main(String[] args) throws IOException {

        if (args.length == 2) {

            final String marketFileParam = args[0];
            final BigDecimal loanAmountParam = new BigDecimal(args[1]);

            try {
                Optional<Loan> loan = getLoan(marketFileParam, loanAmountParam);
                if (loan.isPresent()) {
                    Printer.printLoan(loan.get());
                } else {
                    Printer.printLoanNotPossible();
                }
            } catch (IOException e) {
                //It should use a logging library (ej. log4j)
                System.out.println(format("Error loading market data"));
                //throw e;
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
