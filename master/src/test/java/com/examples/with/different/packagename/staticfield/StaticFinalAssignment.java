package com.examples.with.different.packagename.staticfield;

/**
 * Created by gordon on 04/01/2017.
 */
public class StaticFinalAssignment {

    public static final String FOO = "foo";

    public boolean testMe() {
        if(FOO.equals("foo"))
            return true;
        else
            return false;
    }
}
