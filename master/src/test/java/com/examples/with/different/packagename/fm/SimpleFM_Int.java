package com.examples.with.different.packagename.fm;

/**
 * Created by Andrea Arcuri on 10/08/15.
 */
public class SimpleFM_Int {

    public static interface Foo{
        int foo();
    }

    public static boolean bar(Foo foo){
        if(foo.foo() == 42){
            return true;
        } else {
            return false;
        }
    }
}
