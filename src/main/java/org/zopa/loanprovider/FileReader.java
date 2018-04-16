package org.zopa.loanprovider;


import java.io.*;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reader component to map a lenders list.
 */
public class FileReader {

    private static final String COMMA = ",";

    /**
     * Read data from file I/O saved on filesystem.
     * Because it is a simple console app, assumes it wouldn't necessary use some Async and non-Blocking I/O implementation.
     * However in case of handle lots of data and/or a different data source async mechanics will be a must.
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    public static List<LenderData> getMarketData(String filePath) throws IOException {
        List<LenderData> lenders;
        File file = new File(filePath);
        InputStream inputFS = new FileInputStream(file);
        BufferedReader buffer = new BufferedReader(new InputStreamReader(inputFS));
        lenders = buffer.lines()
                .skip(1) //Skip Header
                .map(mapToLender)
                .collect(Collectors.toList());
        buffer.close();
        return lenders;
    }

    private static final ThrowingFunction<String, LenderData> mapToLender = (line) -> {
        String[] values = line.split(COMMA);
        if (values.length == 3) {
            String name = values[0];
            BigDecimal rate = new BigDecimal(values[1]);
            BigDecimal amount = new BigDecimal(values[2]);
            return new LenderData(name, rate, amount);
        } else {
            throw new ParseException("Error reading market data file.", 0);
        }
    };

    @FunctionalInterface
    public interface ThrowingFunction<T, R> extends Function<T, R> {

        @Override
        default R apply(T t) {
            try {
                return applyThrows(t);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        R applyThrows(T t) throws ParseException;
    }
}
