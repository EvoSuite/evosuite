package com.examples.with.different.packagename.jee;

/**
 * Created by Andrea Arcuri on 14/07/15.
 */
public class InjectionSimpleInheritance extends InjectionAndPostConstruct{

    public InjectionSimpleInheritance(){
        System.out.println("Called constructor");
    }

    public void foo(){
        super.checkObject();
        System.out.println("All OK");
    }
}
