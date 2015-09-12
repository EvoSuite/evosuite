package com.examples.with.different.packagename.mock.java.lang;

/**
 * Created by Andrea Arcuri on 19/08/15.
 */
public class SourceExceptions {

    public static class Foo{

        public static void foo(){
            throw new IllegalArgumentException();
        }
    }


    public void bar(Integer x){
        x.byteValue();
        Foo.foo();
    }
}
