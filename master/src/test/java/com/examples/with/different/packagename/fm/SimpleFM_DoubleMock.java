package com.examples.with.different.packagename.fm;

/**
 * Created by Andrea Arcuri on 10/08/15.
 */
public class SimpleFM_DoubleMock {

    public static interface Foo{
        boolean foo();
    }

    public static interface Bar{
        boolean bar();
    }

    public static boolean something(Foo foo, Bar bar){
        if(foo.foo() && bar.bar()){
            return true;
        } else {
            return false;
        }
    }
}
