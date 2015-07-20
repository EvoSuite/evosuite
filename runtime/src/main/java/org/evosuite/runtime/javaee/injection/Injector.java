package org.evosuite.runtime.javaee.injection;

import org.evosuite.runtime.PrivateAccess;
import org.evosuite.runtime.annotation.BoundInputVariable;
import org.evosuite.runtime.annotation.Constraints;
import org.evosuite.runtime.annotation.EvoSuiteExclude;
import org.evosuite.runtime.javaee.db.DBManager;
import org.evosuite.runtime.javaee.javax.enterprise.event.EvoEvent;
import org.evosuite.runtime.javaee.javax.transaction.EvoUserTransaction;
import org.evosuite.runtime.util.Inputs;
import org.junit.internal.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.transaction.UserTransaction;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class used to inject fields into tagged JavaEE objects
 *
 * Created by Andrea Arcuri on 30/05/15.
 */
public class Injector {

    private static final Logger logger = LoggerFactory.getLogger(Injector.class);

    /*
        Note: these fields are static because we call the static methods of this class
        directly in the JUnit tests.
        At any rate, they are just caches.
     */

    /**
     * Key -> class name,
     * Value -> @PostConstruct method, or null if none
     */
    private static final Map<String, Method> postConstructCache = new LinkedHashMap<>();


    private static final InjectionCache entityManagerCache =
            new InjectionCache(EntityManager.class, Inject.class, PersistenceContext.class);

    private static final InjectionCache entityManagerFactoryCache =
            new InjectionCache(EntityManagerFactory.class, Inject.class, PersistenceUnit.class);


    private static final InjectionCache userTransactionCache =
            new InjectionCache(UserTransaction.class, Inject.class);

    private static final InjectionCache eventCache =
            new InjectionCache(Event.class, Inject.class);

    //this should be initialized with all the caches declared above
    private static final GeneralInjection generalInjection =
            new GeneralInjection(entityManagerCache, entityManagerFactoryCache,
                    userTransactionCache, eventCache);


    @EvoSuiteExclude
    public static void reset(){
        generalInjection.reset();
        postConstructCache.clear();
    }

    @Constraints(noNullInputs = true, notMutable = true, noDirectInsertion = true)
    public  static <T> void inject(@BoundInputVariable(initializer = true, atMostOnceWithSameParameters = true) T instance,
                                   Class<T> klass, String fieldName, Object value)
            throws IllegalArgumentException, AssumptionViolatedException {

        PrivateAccess.setVariable(klass,instance,fieldName,value,InjectionList.getList());
    }

    @EvoSuiteExclude
    public static List<Field> getGeneralFieldsToInject(Class<?> klass){
        return generalInjection.getFieldsToInject(klass);
    }

    @Constraints(noNullInputs = true, notMutable = true, noDirectInsertion = true)
    public static <T> void injectEntityManager(@BoundInputVariable(initializer = true, atMostOnceWithSameParameters = true) T instance, Class<T> clazz)
            throws IllegalArgumentException{

        Inputs.checkNull(instance,clazz);

        String field = entityManagerCache.getFieldName(clazz);
        assert field != null;

        inject(instance, clazz, field, DBManager.getInstance().getCurrentEntityManager());
    }


    @EvoSuiteExclude
    public static boolean hasEntityManager( Class<?> klass) throws IllegalArgumentException{
        Inputs.checkNull(klass);
        return entityManagerCache.hasField(klass);
    }


    @Constraints(noNullInputs = true, notMutable = true, noDirectInsertion = true)
    public static <T> void injectEntityManagerFactory(@BoundInputVariable(initializer = true, atMostOnceWithSameParameters = true) T instance, Class<T> clazz)
            throws IllegalArgumentException{

        Inputs.checkNull(instance,clazz);

        String field = entityManagerFactoryCache.getFieldName(clazz);
        assert field != null;

        inject(instance, clazz, field, DBManager.getInstance().getDefaultFactory());
    }


    @EvoSuiteExclude
    public static boolean hasEntityManagerFactory( Class<?> klass) throws IllegalArgumentException{
        Inputs.checkNull(klass);
        return entityManagerFactoryCache.hasField(klass);
    }


    @Constraints(noNullInputs = true, notMutable = true, noDirectInsertion = true)
    public static <T> void injectUserTransaction(@BoundInputVariable(initializer = true, atMostOnceWithSameParameters = true) T instance, Class<T> clazz)
        throws IllegalArgumentException{

        Inputs.checkNull(instance,clazz);

        String field = userTransactionCache.getFieldName(clazz);
        assert field != null;

        inject(instance, clazz, field, new EvoUserTransaction()); //TODO this will likely need to change in the future
    }


    @EvoSuiteExclude
    public static boolean hasUserTransaction( Class<?> klass) throws IllegalArgumentException{
        Inputs.checkNull(klass);
        return userTransactionCache.hasField(klass);
    }

    @Constraints(noNullInputs = true, notMutable = true, noDirectInsertion = true)
    public static <T> void injectEvent(@BoundInputVariable(initializer = true, atMostOnceWithSameParameters = true) T instance, Class<T> clazz)
            throws IllegalArgumentException{

        Inputs.checkNull(instance, clazz);

        String field = eventCache.getFieldName(clazz);
        assert field != null;

        inject(instance, clazz, field, new EvoEvent()); //TODO this will likely need to change in the future
    }


    @EvoSuiteExclude
    public static boolean hasEvent( Class<?> klass) throws IllegalArgumentException{
        Inputs.checkNull(klass);
        return eventCache.hasField(klass);
    }

    @Constraints(noNullInputs = true, notMutable = true, noDirectInsertion = true)
    public static void executePostConstruct(
            @BoundInputVariable(initializer = true, atMostOnce = true) Object instance) throws IllegalArgumentException {

        Inputs.checkNull(instance);
        executePostConstruct(instance, instance.getClass());
    }

    /**
     * Executed the method annotated with @PostConstruct
     *
     * @param instance
     */
    @Constraints(noNullInputs = true, notMutable = true, noDirectInsertion = true)
    public static void executePostConstruct(
            @BoundInputVariable(initializer = true, atMostOnceWithSameParameters = true) Object instance, Class<?> clazz) throws IllegalArgumentException{

        Inputs.checkNull(instance, clazz);
        if(!clazz.isAssignableFrom(instance.getClass())){
            throw new IllegalArgumentException("Class "+clazz+" is not assignable from "+instance.getClass());
        }
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
            outer : for(Method m : clazz.getDeclaredMethods()){
                for(Annotation annotation : m.getDeclaredAnnotations()){
                    if(annotation instanceof PostConstruct){
                        pc = m;
                        pc.setAccessible(true);
                        break outer;
                    }
                }
            }
            postConstructCache.put(className,pc); //note: it can be null
        }

        Method m = postConstructCache.get(className);
        return m != null;
    }
}
