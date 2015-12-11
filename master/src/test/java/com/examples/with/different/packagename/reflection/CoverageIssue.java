package com.examples.with.different.packagename.reflection;

import java.util.Random;

/**
 * Created by foo on 11/12/15.
 */
public class CoverageIssue {

    private static Random rnd = new Random();

    public static boolean getNextBoolean(double prob) {
        return rnd.nextBoolean();
    }

    public static int nextPos(int n) {
        int nn = rnd.nextInt(n*(n+1)/2) + 1;

        int i;
        for (i=1;(i<=n) && (i*(i-1)/2<nn); i++) { }
        return i-1;
    }

}
