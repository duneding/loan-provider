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
    public static final Comparator<LenderData> byRate = (LenderData left, LenderData right) -> {
        if (left.getRate().equals(right.getRate())) {
            return left.getAvailable().compareTo(right.getAvailable());
        } else {
            return left.getRate().compareTo(right.getRate());
        }
    };

}
