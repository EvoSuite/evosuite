package com.examples.with.different.packagename.jee;

import javax.inject.Inject;

/**
 * Created by Andrea Arcuri on 14/07/15.
 */
public class GeneralInjectionExample {

    public static class A{}
    public static class B{}

    @Inject
    private A a;

    @Inject
    private B b;

    public void foo(){
        a.toString();
        b.toString();
        System.out.println("Successful injection");
    }
}
