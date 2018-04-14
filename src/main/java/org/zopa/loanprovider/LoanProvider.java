package org.zopa.loanprovider;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.Optional;

import static java.text.MessageFormat.format;

/**
 * Created by mdagostino on 4/13/18.
 */
public class LoanProvider {

    private static int MONTHS = 36;

    public static void main(String[] args) {

        if (args.length == 2) {
            final String marketFileParam = args[0];
            final BigDecimal loanAmountParam = new BigDecimal(args[1]);

            Optional<Loan> loan = getLoan(marketFileParam, loanAmountParam);
            loan.ifPresent(LoanProvider::printLoan);

            if (!loan.isPresent()) {
                printLoanNotPossible();
            }

        } else {
            throw new InvalidParameterException("Parameters provided must be 2: [market file] [loan amount]");
        }

    }

    public static void printLoanNotPossible() {
        System.out.println("The market does not have sufficient offers from lenders to satisfy the loan");
    }

    public static void printLoan(Loan loan) {
        System.out.println(format("Requested amount: £{0}", loan.getRequestedAmount()));
        System.out.println(format("Rate: {0}%", loan.getRate()));
        System.out.println(format("Monthly repayment: £{0}", loan.getMonthlyRepayment()));
        System.out.println(format("Total repayment: £{0}", loan.getTotalRepayment()));
    }

    public static Optional<Loan> getLoan(String marketFile, BigDecimal amount) {
        MarketDataProcessor processor = new MarketDataProcessor(marketFile);
        return processor.findLoanFor(amount, MONTHS);
    }

}
