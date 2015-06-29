package org.evosuite.testcase.jee;

import org.evosuite.runtime.javaee.injection.Injector;
import org.evosuite.utils.GenericMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Andrea Arcuri on 29/06/15.
 */
public class InjectionSupport {

    private static final Logger logger = LoggerFactory.getLogger(InjectionSupport.class);

    private static volatile GenericMethod entityManager;

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

}
