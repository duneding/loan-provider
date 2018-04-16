package org.zopa.loanprovider;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.zopa.loanprovider.HelpersTest.*;

/**
 * Test all console message: errors and normal result
 */
public class ConsoleMessageTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

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
    public void testAritmeticError() throws IOException {
        String data = "Lender,Rate,Available\nBob,0.0,6400";
        String[] args = {createTemporalFile(data), "1000"};
        assertErrorConsoleMessages("Error calculating loan amortization for rate", args);
    }

    @Test
    public void testNotEnoughMoneyToSatisfy() throws IOException {
        String data = "Lender,Rate,Available\nBob,0.0,640";
        String[] args = {createTemporalFile(data), "1000"};
        assertOutConsoleMessages("The market does not have sufficient offers from lenders to satisfy the loan", args);
    }

}
