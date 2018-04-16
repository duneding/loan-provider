package org.zopa.loanprovider;

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

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testLoanBaseCase() throws IOException {
        List<LenderData> marketData = new ArrayList<LenderData>() {{
            add(new LenderData("Martin", BigDecimal.valueOf(0.075), BigDecimal.valueOf(650)));
            add(new LenderData("Peter", BigDecimal.valueOf(0.069), BigDecimal.valueOf(480)));
            add(new LenderData("John", BigDecimal.valueOf(0.071), BigDecimal.valueOf(520)));
        }};
        Optional<Loan> loan = Main.getLoan(marketData, new BigDecimal(1000));
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
    public void testInvalidAmountInputResultEmpty() throws IOException {
        Optional<Loan> loan = Main.getLoan(new ArrayList<>(), new BigDecimal(113));
        assertThat(loan).isEmpty();
    }

    @Test
    public void testEmptyMarket() {
        List<LenderData> marketData = new ArrayList<>();
        LoanProcessor loanProcessor = new LoanProcessor(marketData, new FrenchAmortizationMethod());
        assertThat(loanProcessor.findLoanFor(BigDecimal.valueOf(800), 36)).isEmpty();
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
    public void testFrenchAmortizationMethod() {
        AmortizationMethod amortizationMethod = new FrenchAmortizationMethod();
        BigDecimal rate = new BigDecimal(0.05);
        BigDecimal amount = new BigDecimal(1000);
        BigDecimal payment = amortizationMethod.calculateMonthlyPayment(rate, amount, 36);
        assertThat(payment.setScale(2, BigDecimal.ROUND_CEILING)).isEqualTo(BigDecimal.valueOf(29.98));
    }

}
