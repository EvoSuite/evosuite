package org.evosuite.runtime.javaee.db;

import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Class used to setup the in memory database.
 *
 * <p>
 *     See https://objectpartners.com/2010/11/09/unit-testing-your-persistence-tier-code/
 * </p>
 *
 * Created by Andrea Arcuri on 31/05/15.
 */
public class DBManager {

    private static final Logger logger = LoggerFactory.getLogger(DBManager.class);

    /**
     * JPA unit hardcoded in persistence.xml
     */
    private static final String EVOSUITE_DB = "EvoSuiteDB";


    private static final DBManager singleton = new DBManager();

    private final EntityManagerFactory factory;
    private final EntityManager em;

    private DBManager(){
        //TODO inside any DB call should be not instrumentation. although partially handled in
        //     getPackagesShouldNotBeInstrumented, should still disable/enable in method wrappers.
        //     Maybe not needed during test generation, but likely in runtime when JUnit are run in isolation
        //TODO wrapper for EntityManagerFactory: goal of keeping track of what is called on EntityManager.
        //TODO Also keep track of created managers
        factory = Persistence.createEntityManagerFactory(EVOSUITE_DB);
        em = factory.createEntityManager();
    }

    public static DBManager getInstance(){
        return singleton;
    }

    public EntityManagerFactory getDefaultFactory(){
        return factory;
    }

    public EntityManager getDefaultEntityManager(){
        return em;
    }

    public boolean clearDatabase() {
        try {
            //code adapted from https://objectpartners.com/2010/11/09/unit-testing-your-persistence-tier-code/

            Connection c = ((SessionImpl) em.getDelegate()).connection();
            Statement s = c.createStatement();
            s.execute("SET DATABASE REFERENTIAL INTEGRITY FALSE");
            Set<String> tables = new LinkedHashSet<>();
            ResultSet rs = s.executeQuery("select table_name " +
                    "from INFORMATION_SCHEMA.system_tables " +
                    "where table_type='TABLE' and table_schem='PUBLIC'");
            while (rs.next()) {
                if (!rs.getString(1).startsWith("DUAL_")) {
                    tables.add(rs.getString(1));
                }
            }
            rs.close();
            for (String table : tables) {
                s.executeUpdate("DELETE FROM " + table);
            }
            s.execute("SET DATABASE REFERENTIAL INTEGRITY TRUE");
            s.close();
            return true;
        } catch (Exception e){
            logger.error("Failed to clear database: "+e.toString(),e);
            return false;
        }
    }

    /**
     * Be sure the database is ready to use.
     * This means for example rolling back any activate transaction and delete all tables
     */
    public void initDB(){
        if(em.getTransaction().isActive()){
            //TODO should do the same for all other initialized managers
            em.getTransaction().rollback();
        }
        clearDatabase();
    }
}
