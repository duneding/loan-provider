package org.zopa.loanprovider;

import java.util.Comparator;

/**
 * Comparator.
 * Now just provide one (byRate), but it possible consider others variables to evaluate the best offer.
 */
public class LoanComparator {

    /**
     * Comparator of rate ascending to offer best loan.
     */
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
