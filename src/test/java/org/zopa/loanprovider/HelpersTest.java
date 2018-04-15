package org.zopa.loanprovider;

import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
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
        MarketDataProcessor marketDataProcessor = new MarketDataProcessor(marketData);
        Optional<Loan> loan = marketDataProcessor.findLoanFor(amountRequestedExpected, 36);

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

    public static void assertErrorConsoleMessages(String error, String[] args) throws IOException, ParseException {
        StringBuffer sb = new StringBuffer();
        sb.append(error);
        String expectedErrorMessage = sb.toString();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(outContent));
        Main.main(args);
        assertThat(outContent.toString()).contains(expectedErrorMessage);
    }

}
