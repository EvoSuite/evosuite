package com.examples.with.different.packagename.assertion;

/**
 * Created by gordon on 03/02/2016.
 */
public class DoubleWrapperExample {

    public double fooPrimitive(double x) { return x + 0.05; }

    public Double fooWrapper(Double x) { return x + 0.005; }

    public double fooWrapperMixed(Double x) { return x + 0.005; }

    public Double fooWrapperMixed(double x, Double y) { return x + y; }
}
