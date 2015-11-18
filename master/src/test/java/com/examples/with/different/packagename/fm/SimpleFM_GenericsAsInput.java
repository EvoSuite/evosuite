package com.examples.with.different.packagename.fm;

/**
 * Created by Andrea Arcuri on 08/11/15.
 */
public class SimpleFM_GenericsAsInput {

    public static interface Foo<T>{
        public boolean isValid(T t);
    }

    public void bar(Foo<String> bar){
        if(bar.isValid("A")){
            System.out.println("true");
        } else {
            System.out.println("false");
        }
    }
}
