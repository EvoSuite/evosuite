package com.examples.with.different.packagename.dse;

/**
 * Simple example of local class usage
 *
 * @author Ignacio Lebrero
 */
public class LocalClassExample {
    public static int validateNumber(int number) {

        class numberChecker {
            public int isArbitraryNumber(int number) {
                if (number == 48576693)
                    return 1;
                else
                    return 0;
            }
        }

        numberChecker myNumber = new numberChecker();
        return myNumber.isArbitraryNumber(number);
    }
}