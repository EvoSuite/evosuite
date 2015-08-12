package com.examples.with.different.packagename.jee.injection;

import javax.annotation.PostConstruct;

/**
 * Created by Andrea Arcuri on 08/07/15.
 */
public class PostConstructorInjection {

    @PostConstruct
    private void init(){
        System.out.println("Initialized");
    }
}
