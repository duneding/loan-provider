package org.zopa.loanprovider;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test loan calculator, data processor, file parsing, offers comparators.
 */

public class LoanProviderTest {

    private String filePath;

    @Before
    public void setUp() {
        filePath = getClass().getClassLoader().getResource("market.csv").getFile();
    }

    private void assertLoanFor(List<LenderData> marketData, BigDecimal amountRequestedExpected) {
        assertLoanFor(marketData, null, amountRequestedExpected);
    }

    private void assertLoanFor(List<LenderData> marketData, BigDecimal rateExpected, BigDecimal amountRequestedExpected) {
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

    private BigDecimal roundRate(BigDecimal rate) {
        return rate.multiply(BigDecimal.valueOf(100)).setScale(1, BigDecimal.ROUND_CEILING);
    }

    private BigDecimal roundPayment(BigDecimal payment) {
        return payment.setScale(2, BigDecimal.ROUND_CEILING);
    }

    @Test
    public void testMainBaseCaseWithFile() throws IOException {
        Optional<Loan> loan = Main.getLoan(filePath, new BigDecimal(1000));
        assertThat(loan.isPresent()).isTrue();
        BigDecimal rate = roundRate(loan.get().getRate());
        BigDecimal monthlyPayment = roundPayment(loan.get().getMonthlyRepayment());
        BigDecimal totalRepayment = roundPayment(loan.get().getTotalRepayment());
        assertThat(rate).isEqualTo(BigDecimal.valueOf(7.1));
        assertThat(monthlyPayment).isEqualTo(BigDecimal.valueOf(30.97));
        assertThat(loan.get().getRequestedAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(totalRepayment).isEqualTo(BigDecimal.valueOf(1114.88));
    }

    @Test
    public void testFileReaderWithData() throws IOException {
        List<LenderData> lenders = FileReader.getMarketData(filePath);
        assertThat(lenders.size()).isEqualTo(7);
        assertThat(lenders.get(0).getName()).isNotEmpty();
        assertThat(lenders.get(0).getRate()).isGreaterThan(new BigDecimal(0));
        assertThat(lenders.get(0).getAvailable()).isNotNull();
    }

    @Test(expected = IOException.class)
    public void testFileReaderInvalidPath() throws IOException {
        FileReader.getMarketData("path/invalid");
    }

    @Test
    public void testEmptyMarket() {
        List<LenderData> marketData = new ArrayList<>();
        MarketDataProcessor marketDataProcessor = new MarketDataProcessor(marketData);
        assertThat(marketDataProcessor.findLoanFor(BigDecimal.valueOf(800), 36)).isEmpty();
    }

    @Test
    public void testNotEnoughOffer() {
        BigDecimal amountRequestedExpected = BigDecimal.valueOf(8000);
        List<LenderData> marketData = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", BigDecimal.valueOf(0.05), BigDecimal.valueOf(3000)));
        }};

        assertLoanFor(marketData, amountRequestedExpected);
    }

    @Test
    public void testJustOneLenderWithOffer() {
        BigDecimal rateExpected = BigDecimal.valueOf(5.0);
        BigDecimal amountRequestedExpected = BigDecimal.valueOf(1500);
        List<LenderData> marketData = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", BigDecimal.valueOf(0.05), BigDecimal.valueOf(3000)));
        }};

        assertLoanFor(marketData, rateExpected, amountRequestedExpected);
    }

    @Test
    public void testMoreThanOneLenderWithOffer() {
        BigDecimal rateExpected = BigDecimal.valueOf(2.0);
        BigDecimal amountRequestedExpected = BigDecimal.valueOf(1500);
        List<LenderData> marketData = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", BigDecimal.valueOf(0.03), BigDecimal.valueOf(400)));
            add(new LenderData("Peter", BigDecimal.valueOf(0.06), BigDecimal.valueOf(60)));
            add(new LenderData("John", BigDecimal.valueOf(0.02), BigDecimal.valueOf(1200)));
        }};

        assertLoanFor(marketData, rateExpected, amountRequestedExpected);
    }

    @Test
    public void testMoreThanOneLenderWithOfferWithLowBalance() {
        BigDecimal rateExpected = BigDecimal.valueOf(2.0);
        BigDecimal amountRequestedExpected = BigDecimal.valueOf(15000);
        List<LenderData> marketData = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", BigDecimal.valueOf(0.03), BigDecimal.valueOf(0.5)));
            add(new LenderData("Peter", BigDecimal.valueOf(0.06), BigDecimal.valueOf(0.5)));
            add(new LenderData("John", BigDecimal.valueOf(0.02), BigDecimal.valueOf(14999)));
        }};

        assertLoanFor(marketData, rateExpected, amountRequestedExpected);
    }

    @Test
    public void testSeveralLendersButNotEnough() throws Exception {
        BigDecimal amountRequestedExpected = BigDecimal.valueOf(14000);
        List<LenderData> marketData = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", BigDecimal.valueOf(0.03), BigDecimal.valueOf(40)));
            add(new LenderData("Peter", BigDecimal.valueOf(0.06), BigDecimal.valueOf(60)));
            add(new LenderData("John", BigDecimal.valueOf(0.02), BigDecimal.valueOf(120)));
            add(new LenderData("Max", BigDecimal.valueOf(0.03), BigDecimal.valueOf(4)));
            add(new LenderData("Mary", BigDecimal.valueOf(0.06), BigDecimal.valueOf(600)));
            add(new LenderData("Anna", BigDecimal.valueOf(0.02), BigDecimal.valueOf(500)));
        }};

        assertLoanFor(marketData, amountRequestedExpected);
    }

    @Test
    public void testLoanComparator() {
        List<LenderData> data = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", BigDecimal.valueOf(0.06), BigDecimal.valueOf(4320)));
            add(new LenderData("Peter", BigDecimal.valueOf(0.03), BigDecimal.valueOf(608)));
            add(new LenderData("Anna", BigDecimal.valueOf(0.05), BigDecimal.valueOf(1200)));
        }};

        assertThat(data.get(0).getName()).isEqualTo("Martin");
        assertThat(data.get(1).getName()).isEqualTo("Peter");
        assertThat(data.get(2).getName()).isEqualTo("Anna");

        data.sort(LoanComparator.byRate);

        assertThat(data.get(0).getName()).isEqualTo("Peter");
        assertThat(data.get(1).getName()).isEqualTo("Anna");
        assertThat(data.get(2).getName()).isEqualTo("Martin");

    }

}
