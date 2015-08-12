package com.examples.with.different.packagename.jee.injection;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.UserTransaction;

/**
 * Created by Andrea Arcuri on 08/07/15.
 */
public class MultipleDefaultInjections {

    @PersistenceContext
    private EntityManager em;

    @PersistenceUnit
    private EntityManagerFactory factory;

    @Inject
    private UserTransaction userTransaction;

    @Inject
    private Event event;


    public void foo(){
        //those throw exceptions if not injected
        em.toString();
        factory.toString();
        userTransaction.toString();
        event.toString();

        System.out.println("Successful injection");
    }
}
