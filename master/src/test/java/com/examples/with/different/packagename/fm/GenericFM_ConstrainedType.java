package com.examples.with.different.packagename.fm;

/**
 * Created by gordon on 19/04/2017.
 */
public class GenericFM_ConstrainedType {
    public static interface Foo<T extends Number> {
        String foo(T parameter);
    }

    public static boolean bar(Foo<Integer> foo){
        if(foo.foo(10).equals("Bar")){
            return true;
        } else {
            return false;
        }
    }
}
