package com.examples.with.different.packagename.jee.injection;

import javax.inject.Inject;

/**
 * Created by Andrea Arcuri on 20/08/15.
 */
public class InjectionInInputParameter {

    public static class Bar{
        public int get(){return 42;}
    }


    public static class Foo{
        @Inject
        private Bar bar;

        public  int getInt(){
            return bar.get();
        }
    }


    @Inject
    private Foo foo;

    public void exe(){
        if(foo.getInt() == 42){
            System.out.println("Got it");
        }
    }
}
