package com.examples.with.different.packagename.fm;

/**
 * Created by Andrea Arcuri on 09/08/15.
 */
public class SimpleFM_Boolean {

    public static interface Foo{
        boolean foo();
    }

    public static boolean bar(Foo foo){
        if(foo.foo()){
            return true;
        } else {
            return false;
        }
    }
}
