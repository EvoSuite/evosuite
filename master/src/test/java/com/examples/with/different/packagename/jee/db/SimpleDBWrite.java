package com.examples.with.different.packagename.jee.db;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by gordon on 28/04/2017.
 */
public class SimpleDBWrite {
    @PersistenceContext
    private EntityManager em;


    public String persist( String key , String value){

        KVPair_0 pair = new KVPair_0(key, value);

        em.persist(pair);
        KVPair_0 pair2 = em.find(KVPair_0.class , key);
        return pair2.getValue();

    }
}
