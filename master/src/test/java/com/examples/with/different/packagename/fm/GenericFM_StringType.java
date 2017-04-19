package com.examples.with.different.packagename.fm;

/**
 * Created by gordon on 19/04/2017.
 */
public class GenericFM_StringType {

    public static interface Foo<T> {
        String foo(T parameter);
    }

    public static boolean bar(Foo<String> foo){
        if(foo.foo("Test").equals("Bar")){
            return true;
        } else {
            return false;
        }
    }
}
