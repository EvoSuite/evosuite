package com.examples.with.different.packagename.jee;

import javax.inject.Inject;

/**
 * Created by Andrea Arcuri on 16/07/15.
 */
public class InjectionTrivialInheritance extends ASuperClass{

    public void foo(){
        getString().toLowerCase();
        System.out.println("All OK");
    }
}

class ASuperClass {

    @Inject
    private String aString;

    public String getString(){
        return aString;
    }
}
