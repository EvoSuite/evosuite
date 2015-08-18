package com.examples.with.different.packagename.fm;

/**
 * Created by Andrea Arcuri on 18/08/15.
 */
public class SimpleFM_finalClass {

    public static final class Foo{
        public boolean foo(){
            return false;
        }
    }

    public static void bar(Foo foo){
        if(foo.foo()){
            System.out.println("Covered");
        }
    }

}
