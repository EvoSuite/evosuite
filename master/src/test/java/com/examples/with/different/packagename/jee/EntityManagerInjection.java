package com.examples.with.different.packagename.jee;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by Andrea Arcuri on 29/06/15.
 */
public class EntityManagerInjection {

    @PersistenceContext
    private EntityManager em;

    public void foo(){
        em.isOpen(); //if not injected, it ll throw a NPE
        System.out.println("EntityManager was successfully injected");
    }
}
