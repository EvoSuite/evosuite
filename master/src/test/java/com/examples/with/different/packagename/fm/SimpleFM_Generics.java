package com.examples.with.different.packagename.fm;

/**
 * Created by Andrea Arcuri on 18/08/15.
 */
public class SimpleFM_Generics {

    public static interface Foo<T>{
        public T getValue();
    }

    public void bar(Foo<String> bar){
        if(bar.getValue().contains("Bar")){
            System.out.println(bar.getValue());
        }
    }
}
