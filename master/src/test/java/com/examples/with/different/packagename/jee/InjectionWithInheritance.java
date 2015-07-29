package com.examples.with.different.packagename.jee;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by Andrea Arcuri on 14/07/15.
 */
public class InjectionWithInheritance extends InjectionAndPostConstruct {

    @PersistenceContext
    private EntityManager em;

    @Inject
    private Event event;

    @PostConstruct
    private void init() {
        super.checkObject(); //will fail if super PostConstruct is not called first
        em.toString();
        event.toString();
        System.out.println("All is fine");
    }
}