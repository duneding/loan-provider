package org.zopa.loanprovider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zopa.loanprovider.HelpersTest.createTemporalFile;

/**
 * Test all reader functionality
 */
public class FileReaderTest {

    private String marketDataFilePath;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        marketDataFilePath = getClass().getClassLoader().getResource("market.csv").getFile();
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

}
