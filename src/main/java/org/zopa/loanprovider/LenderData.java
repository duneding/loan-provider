package org.zopa.loanprovider;

import java.math.BigDecimal;

/**
 * DTO for lenders data.
 *
 */
public class LenderData {

    //It is assumed that values/lines always comes with data
    private final String name;
    private final BigDecimal rate;
    private final BigDecimal available;

    public LenderData(String name, BigDecimal rate, BigDecimal available) {
        this.name = name;
        this.rate = rate;
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getAvailable() {
        return available;
    }
}
