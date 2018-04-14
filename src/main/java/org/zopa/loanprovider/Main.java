package org.zopa.loanprovider;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mdagostino on 4/13/18.
 */
public class Main {

    private static int months = 36; //Default
    private final static String CONFIG_FILE = "config.properties";
    private final static Logger logger = Logger.getLogger("consoleAppender");

    public static void main(String[] args) {

        if (args.length == 2) {

            Properties properties = getConfiguration();
            months = Integer.valueOf(properties.get("months").toString());

            final String marketFileParam = args[0];
            final BigDecimal loanAmountParam = new BigDecimal(args[1]);

            Optional<Loan> loan = getLoan(marketFileParam, loanAmountParam);

            if (loan.isPresent()) {
                Printer.printLoan(loan.get());
            } else {
                Printer.printLoanNotPossible();
            }

        } else {
            throw new InvalidParameterException("Parameters provided must be 2: [market file] [loan amount]");
        }

    }

    public static Optional<Loan> getLoan(String marketFile, BigDecimal amount) {
        MarketDataProcessor processor = new MarketDataProcessor(marketFile);
        return processor.findLoanFor(amount, months);
    }

    private static Properties getConfiguration() {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            final String filePath = Main.class.getClassLoader().getResource(CONFIG_FILE).getFile();
            input = new FileInputStream(filePath);
            prop.load(input);
        } catch (FileNotFoundException ex) {
            logger.log(Level.CONFIG, "Configuration file not found", ex);
        } catch (IOException ex) {
            logger.log(Level.CONFIG, "I/O error", ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return prop;
    }

}
