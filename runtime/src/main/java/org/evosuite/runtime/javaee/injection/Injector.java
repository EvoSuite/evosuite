package org.evosuite.runtime.javaee.injection;

import org.evosuite.runtime.PrivateAccess;
import org.evosuite.runtime.annotation.BoundInputVariable;
import org.evosuite.runtime.annotation.Constraints;
import org.evosuite.runtime.annotation.EvoSuiteExclude;
import org.junit.internal.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class used to inject fields into tagged JavaEE objects
 *
 * Created by Andrea Arcuri on 30/05/15.
 */
public class Injector {

    private static final Logger logger = LoggerFactory.getLogger(Injector.class);

    private static final Map<String, Method> postConstructCache = new LinkedHashMap<>();

    @Constraints(noNullInputs = true, notMutable = true, noDirectInsertion = true)
    public  static <T> void inject(@BoundInputVariable(initializer = true) T instance,
                                   Class<T> klass, String fieldName, Object value)
            throws IllegalArgumentException, AssumptionViolatedException {

        PrivateAccess.setVariable(klass,instance,fieldName,value,InjectionList.getList());
    }

    /**
     * Executed the method annotated with @PostConstruct
     *
     * @param instance
     */
    @Constraints(noNullInputs = true, notMutable = true, noDirectInsertion = true)
    public static void executePostConstruct(@BoundInputVariable(initializer = true) Object instance) throws IllegalArgumentException{

        if(instance == null){
            throw new IllegalArgumentException("Null input parameter");
        }

        Class<?> clazz = instance.getClass();
        if(!hasPostConstruct(clazz)){
            throw new IllegalArgumentException("The class "+clazz.getName()+" does not have a @PostConstruct");
        }

        Method m = postConstructCache.get(clazz.getName());
        assert m != null;

        try {
            m.invoke(instance);
        } catch (IllegalAccessException e) {
            //should never happen
            logger.error(e.toString());
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to execute @PostConstructor in "+clazz.getName(),e);
        }
    }

    @EvoSuiteExclude
    public static boolean hasPostConstruct(Class<?> clazz){
        String className = clazz.getName();
        if(! postConstructCache.containsKey(className)){
            Method pc = null;
            for(Method m : clazz.getDeclaredMethods()){
                for(Annotation annotation : m.getDeclaredAnnotations()){
                    if(annotation instanceof PostConstruct){
                        pc = m;
                        pc.setAccessible(true);
                        break;
                    }
                }
            }
            postConstructCache.put(className,pc); //note: it can be null
        }

        Method m = postConstructCache.get(className);
        return m != null;
    }
}
