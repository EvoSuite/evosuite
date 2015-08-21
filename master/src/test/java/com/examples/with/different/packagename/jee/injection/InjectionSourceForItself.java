package com.examples.with.different.packagename.jee.injection;

import javax.inject.Inject;

/**
 * Created by Andrea Arcuri on 20/08/15.
 */
public class InjectionSourceForItself {

    public static class Bar{
        public int get(){return 42;}
    }


    public static class Foo{
        @Inject
        private Bar bar;

        public  Bar getBar(){
            return bar;
        }
    }


    @Inject
    private Foo foo;

    public void exe(){
        if(foo.getBar().get() == 42){
            System.out.println("Got it");
        }
    }
}
