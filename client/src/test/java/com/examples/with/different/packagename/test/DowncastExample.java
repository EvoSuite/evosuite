package com.examples.with.different.packagename.test;

/**
 * Created by gordon on 27/12/2016.
 */
public class DowncastExample {

    public Number getANumber(int x) {
        if(x == 42) {
            return new Integer(42);
        } else {
            return new Double(0.0);
        }
    }

    public boolean testMe(Number x) {
        if(x instanceof Double) {
            return true;
        } else {
            return false;
        }
    }

    public boolean testWithInteger(Integer x) {
        if(x.equals(42)) {
            return true;
        } else {
            return false;
        }
    }
}
