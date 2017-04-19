package com.examples.with.different.packagename.fm;

/**
 * Created by gordon on 19/04/2017.
 */
public class GenericFM_GenericMethod {
    public static interface Foo {
        <T> T foo(T obj);
    }

    public static boolean bar(Foo foo){
        if(foo.foo("Test").equals("Bar")){
            return true;
        } else {
            return false;
        }
    }
}
