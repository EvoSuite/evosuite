package com.examples.with.different.packagename;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class Error {
    public void checkError(int[] a){
        int error = 0;
        int errorsum = 0;
        for (int i = 0; i < 16; i++) {
            error = a[i] - i;
            errorsum = errorsum + min(1, abs(error));
        }
        if ((errorsum / 4) < 1) { // integer division
            // target
        }
    }
}
