package org.evosuite.runtime.mock.javax.persistence;

import org.evosuite.runtime.mock.OverrideMock;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUtil;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrea Arcuri on 30/05/15.
 */
public class MockPersistence extends Persistence implements OverrideMock{

    /**
     * JPA unit hardcoded in persistence.xml
     */
    private static final String EVOSUITE_DB = "EvoSuiteDB";

    // -------- mocked methods  ----------------------

    public MockPersistence() {
    }

    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName) {
        //TODO wrapper for EntityManagerFactory: goal of keeping track of what is called on EntityManager
        return Persistence.createEntityManagerFactory(EVOSUITE_DB);
    }

    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
        //TODO wrapper for EntityManagerFactory: goal of keeping track of what is called on EntityManager
        return Persistence.createEntityManagerFactory(EVOSUITE_DB,properties);
    }

    public static PersistenceUtil getPersistenceUtil() {
        return Persistence.getPersistenceUtil();
    }

    // -------  EvoSuite methods ---------------------

    public static EntityManagerFactory getDefaultFactory(){
        //TODO properly with cache and reset
        return createEntityManagerFactory("");
    }
}
