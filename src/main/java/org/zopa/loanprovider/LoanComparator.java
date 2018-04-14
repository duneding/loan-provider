package org.zopa.loanprovider;

import java.util.Comparator;

/**
 * Created by mdagostino on 4/13/18.
 */
public class LoanComparator {

    public static Comparator<LenderData> byRate = new Comparator<LenderData>() {
        @Override
        public int compare(LenderData a, LenderData b) {
            if (a.getRate() == b.getRate()) {
                return a.getAvailable().compareTo(b.getAvailable());
            } else {
                return a.getRate().compareTo(b.getRate());
            }
        }
    };

}
