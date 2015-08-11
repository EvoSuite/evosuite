package com.examples.with.different.packagename.jee.injection;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by Andrea Arcuri on 14/07/15.
 */
public class InjectionAndPostConstruct {

    @Inject
    private Event event;

    @Inject
    private String aString;

    private Object obj;

    @PostConstruct
    private void init(){
        event.toString(); //throw exception if not injected
        aString.toString();
        obj = new Object();
        System.out.println("Initialized");
    }

    public void checkObject(){
        obj.toString(); //throw exception if no PostConstruct
    }
}
