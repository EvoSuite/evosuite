package org.evosuite.runtime.javaee.db;

import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate3.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
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

        //factory = Persistence.createEntityManagerFactory(EVOSUITE_DB);
        factory = createEMFWithSpring();


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
                String delete = "DELETE FROM " + table;
                s.executeUpdate(delete);
                logger.debug("SQL executed: "+delete);
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


    private EntityManagerFactory createEMFWithSpring(){

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
        dataSource.setUrl("jdbc:hsqldb:mem:.");
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("**.*"); //search everything on classpath
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties properties = new Properties();
        properties.setProperty("hibernate.show_sql", "true");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        properties.setProperty("hibernate.connection.shutdown", "true");
        properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        em.setJpaProperties(properties);

        em.afterPropertiesSet();

        return em.getObject();
    }


    @Configuration
    @EnableTransactionManagement
    private class StandaloneDataJpaConfig {

        @Bean
        public DataSource dataSource() {

            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.hsqldb.jdbcDriver");
            dataSource.setUrl("jdbc:hsqldb:mem:.");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
            /*
            return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL)
                    .addScript("classpath:sql/schema.sql")
                    .addScript("classpath:sql/import-users.sql")
                    .build();
                    */
        }

        @Bean
        public PlatformTransactionManager transactionManager() {

            JpaTransactionManager txManager = new JpaTransactionManager();
            txManager.setEntityManagerFactory(entityManagerFactory());
            return txManager;
        }

        @Bean
        public HibernateExceptionTranslator hibernateExceptionTranslator() {
            return new HibernateExceptionTranslator();
        }

        @Bean
        public EntityManagerFactory entityManagerFactory() {

            // will set the provider to 'org.hibernate.ejb.HibernatePersistence'
            HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
            // will set hibernate.show_sql to 'true'
            vendorAdapter.setShowSql(true);
            // if set to true, will set hibernate.hbm2ddl.auto to 'update'
            vendorAdapter.setGenerateDdl(false);

            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setJpaVendorAdapter(vendorAdapter);
            factory.setPackagesToScan("com.geowarin.standalonedatajpa.model");
            factory.setDataSource(dataSource());

            // This will trigger the creation of the entity manager factory
            factory.afterPropertiesSet();

            return factory.getObject();
        }
    }
}
