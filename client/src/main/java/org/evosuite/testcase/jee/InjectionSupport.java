package org.evosuite.testcase.jee;

import org.evosuite.runtime.javaee.injection.Injector;
import org.evosuite.utils.generic.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods used to do JEE dependency injection.
 *
 * Created by Andrea Arcuri on 29/06/15.
 */
public class InjectionSupport {

    private static final Logger logger = LoggerFactory.getLogger(InjectionSupport.class);

    private static volatile GenericMethod entityManager;
    private static volatile GenericMethod entityManagerFactory;
    private static volatile GenericMethod userTransaction;
    private static volatile GenericMethod event;
    private static volatile GenericMethod postConstruct;

    public static GenericMethod getPostConstruct(){
        if(postConstruct == null){
            try {
                postConstruct = new GenericMethod(
                        Injector.class.getDeclaredMethod("executePostConstruct",Object.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return postConstruct;
    }

    public static GenericMethod getInjectorForEntityManager(){
        if(entityManager == null){
            try {
                entityManager = new GenericMethod(
                        Injector.class.getDeclaredMethod("injectEntityManager",Object.class,Class.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return entityManager;
    }

    public static GenericMethod getInjectorForEntityManagerFactory(){
        if(entityManagerFactory == null){
            try {
                entityManagerFactory = new GenericMethod(
                        Injector.class.getDeclaredMethod("injectEntityManagerFactory",Object.class,Class.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return entityManagerFactory;
    }

    public static GenericMethod getInjectorForUserTransaction(){
        if(userTransaction == null){
            try {
                userTransaction = new GenericMethod(
                        Injector.class.getDeclaredMethod("injectUserTransaction",Object.class,Class.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return userTransaction;
    }

    public static GenericMethod getInjectorForEvent(){
        if(event == null){
            try {
                event = new GenericMethod(
                        Injector.class.getDeclaredMethod("injectEvent",Object.class,Class.class)
                        , Injector.class);
            } catch (NoSuchMethodException e) {
                logger.error("Reflection failed in InjectionSupport: "+e.getMessage());
                return null;
            }
        }
        return event;
    }

}
