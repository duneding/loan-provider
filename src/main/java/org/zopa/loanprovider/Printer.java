package org.zopa.loanprovider;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.text.MessageFormat.format;

/**
 * Centralize all message interface between users and calculator
 */
public class Printer {

    public static void printLoanNotPossible() {
        System.out.println("The market does not have sufficient offers from lenders to satisfy the loan");
    }

    public static void printLoan(Loan loan) {
        BigDecimal rate = loan.getRate().multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.CEILING);
        BigDecimal monthlyPayment = loan.getMonthlyRepayment().setScale(2, RoundingMode.CEILING);
        BigDecimal totalRepayment = loan.getTotalRepayment().setScale(2, RoundingMode.CEILING);
        System.out.println(format("Requested amount: £{0}", loan.getRequestedAmount()));
        System.out.println(format("Rate: {0}%", rate));
        System.out.println(format("Monthly repayment: £{0}", monthlyPayment));
        System.out.println(format("Total repayment: £{0}", totalRepayment));
    }

    public static void printError(String message) {
        System.out.println(format(message));
    }
}
