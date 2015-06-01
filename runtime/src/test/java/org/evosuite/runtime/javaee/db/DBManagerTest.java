package org.evosuite.runtime.javaee.db;

import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 31/05/15.
 */
public class DBManagerTest {

    @Test
    public void testClearDatabase() throws Exception {

        boolean cleared = false;

        cleared = DBManager.getInstance().clearDatabase();
        Assert.assertTrue(cleared);



        String key = "foo";
        String value = "bar";
        KVPair pair = new KVPair(key,value);

        EntityManager em = DBManager.getInstance().getDefaultEntityManager();

        em.getTransaction().begin();

        KVPair queried = null;

        queried = em.find(KVPair.class,key);
        Assert.assertNull(queried); //not inserted yet

        em.persist(pair);
        queried = em.find(KVPair.class,key);
        Assert.assertNotNull(queried); //now it should be found
        Assert.assertEquals(value, queried.getValue());

        em.getTransaction().commit();

        //clean the db again
        cleared = DBManager.getInstance().clearDatabase();
        Assert.assertTrue(cleared);

        //object shouldn't be in db now
        queried = em.find(KVPair.class, key);
        Assert.assertNull(queried); //n
    }
}