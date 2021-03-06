package org.zopa.loanprovider;

import java.io.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Helpers to handle data for testing.
 */

public class HelpersTest {

    public static void assertLoanFor(List<LenderData> marketData, BigDecimal amountRequestedExpected) {
        assertLoanFor(marketData, null, amountRequestedExpected);
    }

    public static void assertLoanFor(List<LenderData> marketData, BigDecimal rateExpected, BigDecimal amountRequestedExpected) {

        LoanProcessor loanProcessor = new LoanProcessor(marketData, new FrenchAmortizationMethod());
        Optional<Loan> loan = loanProcessor.findLoanFor(amountRequestedExpected, 36);

        if (rateExpected == null) {
            assertThat(loan).isEmpty();
        } else {
            assertThat(loan).isNotEmpty();
            BigDecimal rate = roundRate(loan.get().getRate());
            BigDecimal monthlyPayment = loan.get().getMonthlyRepayment();
            BigDecimal totalRepayment = roundPayment(loan.get().getTotalRepayment());

            assertThat(rate).isEqualTo(rateExpected);
            assertThat(loan.get().getRequestedAmount()).isEqualTo(amountRequestedExpected);
            assertThat(totalRepayment).isGreaterThan(amountRequestedExpected);

            // Sanity check for monthly payment
            assertThat(roundPayment(monthlyPayment.multiply(BigDecimal.valueOf(36)))).isEqualTo(totalRepayment);
        }
    }

    public static BigDecimal roundRate(BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(100)).setScale(1, BigDecimal.ROUND_FLOOR);
    }

    public static BigDecimal roundPayment(BigDecimal payment) {
        return payment.setScale(2, BigDecimal.ROUND_CEILING);
    }

    public static String createTemporalFile() throws IOException {
        return createTemporalFile(null);
    }

    public static String createTemporalFile(String data) throws IOException {
        File temporaryFile = File.createTempFile("market-tmp-", ".csv");

        if (data != null) {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(temporaryFile));
            bufferedWriter.write(data);
            bufferedWriter.close();
        }

        temporaryFile.deleteOnExit();
        return temporaryFile.getPath();
    }

    public static void assertOutConsoleMessages(String message, String[] params) {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        assertConsoleMessages(params, outContent, buildMessageExpected(message));
    }

    public static void assertErrorConsoleMessages(String message, String[] params) {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(outContent));
        assertConsoleMessages(params, outContent, buildMessageExpected(message));
    }

    private static void assertConsoleMessages(String[] params, ByteArrayOutputStream outContent, String expectedMessage) {
        Main.main(params);
        assertThat(outContent.toString()).contains(expectedMessage);
    }

    private static String buildMessageExpected(String message) {
        StringBuilder sb = new StringBuilder().append(message);
        return sb.toString();
    }

}
