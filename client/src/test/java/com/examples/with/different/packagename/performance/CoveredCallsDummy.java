package com.examples.with.different.packagename.performance;

/**
 * Dummy class to test the {@link org.evosuite.performance.indicator.CoveredMethodCallCounter}
 */
public class CoveredCallsDummy {

    public void callOne() {
        callTwo();
    }

    public void callTwo() {
        callThree();
        callThree();
        callThree();
    }

    public void callThree() {
        int one = 1;
        int two = 2;
        int three = 3;
        int four = 4;
        int five = 5;
    }
}
