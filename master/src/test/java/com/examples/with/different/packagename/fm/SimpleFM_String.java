package com.examples.with.different.packagename.fm;

/**
 * Created by Andrea Arcuri on 10/08/15.
 */
public class SimpleFM_String {

    public static interface Foo{
        String foo();
    }

    public static boolean bar(Foo foo){
        if(foo.foo().equals("Bar")){
            return true;
        } else {
            return false;
        }
    }
}
