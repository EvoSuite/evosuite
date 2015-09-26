package com.examples.with.different.packagename.fm;

/**
 * Created by Andrea Arcuri on 20/09/15.
 */
public class SimpleFM_returnString {

    public final static String TRUE_BRANCH = "true branch";
    public final static String FALSE_BRANCH = "false branch";

    public static interface Foo{
        boolean foo();
    }

    public static String bar(Foo foo){
        if(foo.foo()){
            return TRUE_BRANCH;
        } else {
            return FALSE_BRANCH;
        }
    }
}
