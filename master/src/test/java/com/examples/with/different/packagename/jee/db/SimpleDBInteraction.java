package com.examples.with.different.packagename.jee.db;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Created by Andrea Arcuri on 17/08/15.
 */
public class SimpleDBInteraction {

    @PersistenceContext
    private EntityManager em;


    public void persist( String key , String value){

        KVPair pair = new KVPair(key, value);

        em.persist(pair);
    }

    public boolean check(String key, String value){

        KVPair pair = em.find(KVPair.class , key);
        if(pair==null){
            System.out.println("Not found");
            return false;
        }

        if(pair.getValue().equals(value)){
            System.out.println("Match");
            return true;
        } else {
            System.out.println("No match");
            return false;
        }
    }

}
