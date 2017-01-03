package com.examples.with.different.packagename.staticfield;

/**
 * Created by gordon on 30/11/2016.
 */
public class StaticFieldUninitialized {

    private static Object foo;

    private static int bar;

    static {
        setBar(7);
    }

    public static void setFoo(Integer x) {
        foo = x;
    }

    public static void setFoo(String x) {
        foo = x;
    }

    public static void setBar(int x) {
        bar = x;
    }

    public static Object getFoo() {
        return foo;
    }

    public static int getBar() {
        foo = 127;
        return bar;
    }
}
