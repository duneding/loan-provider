package org.zopa.loanprovider;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by mdagostino on 4/13/18.
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
            assertThat(loan.get().getRate()).isEqualTo(rateExpected);
            assertThat(loan.get().getRequestedAmount()).isEqualTo(amountRequestedExpected);
            assertThat(loan.get().getTotalRepayment()).isGreaterThan(amountRequestedExpected);

            // Sanity check for monthly payment
            BigDecimal totalRepayment = loan.get().getTotalRepayment();
            assertThat(loan.get().getMonthlyRepayment().multiply(BigDecimal.valueOf(36))).isEqualTo(totalRepayment);
        }
    }

    @Test
    public void testMainBaseCaseWithFile() throws IOException {
        Optional<Loan> loan = Main.getLoan(filePath, new BigDecimal(1000));
        assertThat(loan.isPresent()).isTrue();
        BigDecimal rate = loan.get().getRate().multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.FLOOR);
        assertThat(rate).isEqualTo(BigDecimal.valueOf(7.0));
        assertThat(loan.get().getMonthlyRepayment().setScale(2, RoundingMode.CEILING)).isEqualTo(BigDecimal.valueOf(30.88));
        assertThat(loan.get().getRequestedAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(loan.get().getTotalRepayment().setScale(2, RoundingMode.CEILING)).isEqualTo(BigDecimal.valueOf(1111.65));
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
        BigDecimal rateExpected = BigDecimal.valueOf(0.05);
        BigDecimal amountRequestedExpected = BigDecimal.valueOf(1500);
        List<LenderData> marketData = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", rateExpected, BigDecimal.valueOf(3000)));
        }};

        assertLoanFor(marketData, rateExpected, amountRequestedExpected);
    }

    @Test
    public void testMoreThanOneLenderWithOffer() {
        BigDecimal rateExpected = BigDecimal.valueOf(0.022);
        BigDecimal amountRequestedExpected = BigDecimal.valueOf(1500);
        List<LenderData> marketData = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", BigDecimal.valueOf(0.03), BigDecimal.valueOf(400)));
            add(new LenderData("Peter", BigDecimal.valueOf(0.06), BigDecimal.valueOf(60)));
            add(new LenderData("John", BigDecimal.valueOf(0.02), BigDecimal.valueOf(1200)));
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

}
