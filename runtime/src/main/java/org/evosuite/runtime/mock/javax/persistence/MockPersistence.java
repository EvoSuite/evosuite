package org.evosuite.runtime.mock.javax.persistence;

import org.evosuite.runtime.javaee.db.DBManager;
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


    // -------- mocked methods  ----------------------

    public MockPersistence() {
    }

    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName) {
        return DBManager.getInstance().getDefaultFactory();
    }

    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map properties) {
        return DBManager.getInstance().getDefaultFactory();    }

    public static PersistenceUtil getPersistenceUtil() {
        return Persistence.getPersistenceUtil();
    }

    // -------  EvoSuite methods ---------------------


}
