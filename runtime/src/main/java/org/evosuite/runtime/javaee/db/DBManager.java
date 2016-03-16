/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime.javaee.db;

import org.evosuite.runtime.javaee.javax.persistence.EvoEntityManagerFactory;
import org.evosuite.runtime.util.AtMostOnceLogger;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
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

    private EvoEntityManagerFactory factory;
    private EntityManager em;

    private boolean wasAccessed;

    /**
     * The SUT classloader used when the database was initialized
     */
    private ClassLoader sutClassLoader;

    private DBManager(){
        //TODO inside any DB call should be not instrumentation. although partially handled in
        //     getPackagesShouldNotBeInstrumented, should still disable/enable in method wrappers.
        //     Maybe not needed during test generation, but likely in runtime when JUnit are run in isolation,
        //     unless we do full shading

        wasAccessed = false;
    }

    public static DBManager getInstance(){
        return singleton;
    }

    public boolean isWasAccessed() {
        return wasAccessed;
    }

    public EntityManagerFactory getDefaultFactory(){
        if(!wasAccessed){
            initDB();
            wasAccessed = true;
        }
        return factory;
    }

    public EntityManager getCurrentEntityManager(){
        if(!wasAccessed){
            initDB();
            wasAccessed = true;
        }
        return em;
    }

    private void createNewEntityManager(){
        em = factory.createEntityManager();
    }

    public boolean clearDatabase() {
        if(!wasAccessed){
            return false;
        }

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
                String delete = "DELETE FROM " + table;
                s.executeUpdate(delete);
                logger.debug("SQL executed: "+delete);
            }
            s.execute("SET DATABASE REFERENTIAL INTEGRITY TRUE");
            s.close();
            return true;
        } catch (Exception e){
            AtMostOnceLogger.error(logger, "Failed to clear database: "+e);
            return false;
        }
    }

    /**
     * Be sure the database is ready to use.
     * This means for example rolling back any activate transaction and delete all tables
     */
    public void initDB(){
        wasAccessed = true;

        if(factory==null){
            factory = new EvoEntityManagerFactory();
            createNewEntityManager();
        } else {
            factory.clearAllEntityManagers();
            if(!factory.isOpen()){
            /*
                this maybe could happen if "close" is called in the SUT.
                note: initializing a factory seems quite expensive, and this is the
                reason why we try here to reuse it instead of creating a new one
                at each new test case run
             */
                factory = new EvoEntityManagerFactory();
            }
            createNewEntityManager();
            clearDatabase();
        }
    }

    public ClassLoader getSutClassLoader() {
        return sutClassLoader;
    }

    public void setSutClassLoader(ClassLoader sutClassLoader) {
        this.sutClassLoader = sutClassLoader;
    }
}
