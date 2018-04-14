package org.zopa.loanprovider;


import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

/**
 * Created by mdagostino on 4/13/18.
 */
public class FileReader {

    private static final String COMMA = ",";

    public static List<LenderData> getMarketData(String filePath) {
        List<LenderData> lenders = new ArrayList<>();
        try {
            File file = new File(filePath);
            InputStream inputFS = new FileInputStream(file);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(inputFS));
            lenders = buffer.lines()
                    .skip(1) //Skip Header
                    .map(mapToLender)
                    .collect(Collectors.toList());
            buffer.close();
        } catch (IOException e) {
            System.out.println(format("Error proccesing market file: {0}", e.getMessage()));
        }

        return lenders;
    }

    private static Function<String, LenderData> mapToLender = (line) -> {
        String[] values = line.split(COMMA);
        String name = values[0];
        BigDecimal rate = new BigDecimal(values[1]);
        BigDecimal amount = new BigDecimal(values[2]);
        LenderData item = new LenderData(name, rate, amount);
        return item;
    };
}
