package org.zopa.loanprovider;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
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

    @Test
    public void testBaseCase() {
        Optional<Loan> loan = LoanProvider.getLoan(filePath, new BigDecimal(1000));
        LoanProvider.printLoan(loan.get());
    }

    @Test
    public void testFileReader() {
        List<LenderData> lenders = FileReader.getMarketData(filePath);
        assertThat(lenders.size()).isEqualTo(7);
        assertThat(lenders.get(0).getName()).isEqualTo("Bob");
        assertThat(lenders.get(0).getRate()).isGreaterThan(new BigDecimal(0));
        assertThat(lenders.get(0).getAvailable()).isNotNull();
    }
}
