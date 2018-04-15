package org.zopa.loanprovider;

import java.math.BigDecimal;
import java.util.Optional;

import static java.text.MessageFormat.format;

/**
 * Centralize all message interface between users and calculator
 */
public class Printer {

    private static void printLoanNotPossible() {
        System.out.println("The market does not have sufficient offers from lenders to satisfy the loan");
    }

    private static void printLoan(Loan loan) {
        BigDecimal rate = loan.getRate().multiply(BigDecimal.valueOf(100)).setScale(1, BigDecimal.ROUND_FLOOR);
        BigDecimal monthlyPayment = loan.getMonthlyRepayment().setScale(2, BigDecimal.ROUND_CEILING);
        BigDecimal totalRepayment = loan.getTotalRepayment().setScale(2, BigDecimal.ROUND_CEILING);
        System.out.println(format("Requested amount: £{0}", loan.getRequestedAmount().toString()));
        System.out.println(format("Rate: {0}%", rate.toString()));
        System.out.println(format("Monthly repayment: £{0}", monthlyPayment.toString()));
        System.out.println(format("Total repayment: £{0}", totalRepayment.toString()));
    }

    public static void printError(String message) {
        System.err.println(format(message));
    }

    public static void printResult(Optional<Loan> loan) {
        if (loan.isPresent()) {
            printLoan(loan.get());
        } else {
            printLoanNotPossible();
        }
    }
}
