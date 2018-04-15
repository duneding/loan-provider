package org.zopa.loanprovider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zopa.loanprovider.HelpersTest.*;

/**
 * Test loan calculator, data processor, file parsing, offers comparators.
 */
public class LoanProviderTest {

    private String marketDataFilePath;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        marketDataFilePath = getClass().getClassLoader().getResource("market.csv").getFile();
    }

    @Test
    public void testMainBaseCaseWithFile() throws IOException {
        Optional<Loan> loan = Main.getLoan(marketDataFilePath, new BigDecimal(1000));
        assertThat(loan.isPresent()).isTrue();
        BigDecimal rate = roundRate(loan.get().getRate());
        BigDecimal monthlyPayment = roundPayment(loan.get().getMonthlyRepayment());
        BigDecimal totalRepayment = roundPayment(loan.get().getTotalRepayment());
        assertThat(rate).isEqualTo(BigDecimal.valueOf(7.0));
        assertThat(monthlyPayment).isEqualTo(BigDecimal.valueOf(30.88));
        assertThat(loan.get().getRequestedAmount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(totalRepayment).isEqualTo(BigDecimal.valueOf(1111.65));
    }

    @Test
    public void testPassingWrongNumberOfInputs() throws IOException {
        String[] args = {"one"};
        assertErrorConsoleMessages("Parameters provided must be 2: [market file] [loan amount]", args);
    }

    @Test
    public void testPassingInvalidIncrementAmountInput() throws IOException {
        String[] args = {"path.mock", "113"};
        assertErrorConsoleMessages("Amount requested must be for 100 increment", args);
    }

    @Test
    public void testPassingInvalidBandAmountInputForHigh() throws IOException {
        String[] args = {"path.mock", "20000"};
        assertErrorConsoleMessages("Amount requested must be between 1000 and 15000", args);
    }

    @Test
    public void testPassingInvalidBandAmountInputForLow() throws IOException {
        String[] args = {"path.mock", "20"};
        assertErrorConsoleMessages("Amount requested must be between 1000 and 15000", args);
    }

    @Test
    public void testPassingNegativeAmount() throws IOException {
        String[] args = {"path.mock", "-1"};
        assertErrorConsoleMessages("Amount requested must be between 1000 and 15000", args);
    }

    @Test
    public void testPassingDecimalAmount() throws IOException {
        String[] args = {"path.mock", "1000.50"};
        assertErrorConsoleMessages("Amount requested must be between 1000 and 15000", args);
    }

    @Test
    public void testPassingInvalidStringAmount() throws IOException {
        exception.expect(NumberFormatException.class);
        String[] args = {"path.mock", "not.valid.number"};
        assertErrorConsoleMessages("Amount parameter must be a number", args);
    }

    @Test
    public void testPassingInvalidPathInput() throws IOException {
        String[] args = {"invalid.path.mock", "1000"};
        assertErrorConsoleMessages("Error loading market data", args);
    }

    @Test
    public void testPassingFilePathCorrupted() throws IOException {
        String dataCorrupted = "Data not valid------....%!··%&/()-----Data not valid----¿?)=()=(\n --Data not valid.";
        String[] args = {createTemporalFile(dataCorrupted), "1000"};
        assertErrorConsoleMessages("Error reading market data file", args);
    }

    @Test
    public void testInvalidAmountInputResultEmpty() throws IOException {
        Optional<Loan> loan = Main.getLoan(marketDataFilePath, new BigDecimal(113));
        assertThat(loan.isPresent()).isTrue();
    }

    @Test
    public void testFileReaderWithData() throws IOException {
        List<LenderData> lenders = FileReader.getMarketData(marketDataFilePath);
        assertThat(lenders.size()).isEqualTo(7);
        assertThat(lenders.get(0).getName()).isNotEmpty();
        assertThat(lenders.get(0).getRate()).isGreaterThan(new BigDecimal(0));
        assertThat(lenders.get(0).getAvailable()).isNotNull();
    }

    @Test
    public void testFileReaderInvalidPath() throws IOException {
        exception.expect(IOException.class);
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
        BigDecimal rateExpected = BigDecimal.valueOf(2.2);
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
    public void testMoreThanOneLenderWithOfferFirst() {
        BigDecimal rateExpected = BigDecimal.valueOf(3.0);
        BigDecimal amountRequestedExpected = BigDecimal.valueOf(1600);
        List<LenderData> marketData = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", BigDecimal.valueOf(0.03), BigDecimal.valueOf(2000)));
            add(new LenderData("Peter", BigDecimal.valueOf(0.06), BigDecimal.valueOf(500)));
            add(new LenderData("John", BigDecimal.valueOf(0.05), BigDecimal.valueOf(1499)));
        }};

        assertLoanFor(marketData, rateExpected, amountRequestedExpected);
    }

    @Test
    public void testSeveralLendersButNotEnough() {
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
    public void testSeveralLendersButAlmostOffer() {
        BigDecimal amountRequestedExpected = BigDecimal.valueOf(1000);
        List<LenderData> marketData = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", BigDecimal.valueOf(0.03), BigDecimal.valueOf(500)));
            add(new LenderData("Peter", BigDecimal.valueOf(0.06), BigDecimal.valueOf(499)));
        }};

        assertLoanFor(marketData, amountRequestedExpected);
    }

    @Test
    public void testSeveralLendersWithAlmostNotEnough() {
        BigDecimal rateExpected = BigDecimal.valueOf(5.0);
        BigDecimal amountRequestedExpected = BigDecimal.valueOf(1000);
        List<LenderData> marketData = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", BigDecimal.valueOf(0.05), BigDecimal.valueOf(500)));
            add(new LenderData("Peter", BigDecimal.valueOf(0.05), BigDecimal.valueOf(501)));
        }};

        assertLoanFor(marketData, rateExpected, amountRequestedExpected);
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

    @Test
    public void testMarketDaraFileCorrupted() throws IOException {
        String dataCorrupted = "Data not valid------....%!··%&/()-----Data not valid----¿?)=()=(\n --Data not valid.";
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error reading market data file");
        FileReader.getMarketData(createTemporalFile(dataCorrupted));
    }

    @Test
    public void testMarketDaraFileEmpty() throws IOException {
        assertThat(FileReader.getMarketData(createTemporalFile())).isEmpty();
    }

}
