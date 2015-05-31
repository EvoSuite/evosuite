package org.evosuite.runtime.mock.javax.persistence;

import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 30/05/15.
 */
public class MockPersistenceTest {

    @Test
    public void testHibernateConfiguration(){
        EntityManagerFactory factory = MockPersistence.createEntityManagerFactory("foo");
        EntityManager em = factory.createEntityManager();
        factory.close();
        Assert.assertNotNull(em);
    }
}