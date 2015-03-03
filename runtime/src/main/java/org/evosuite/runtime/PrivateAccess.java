package org.evosuite.runtime;

import org.junit.internal.AssumptionViolatedException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to access private fields/methods by reflection.
 * If the accessed fields/methods do not exist any more, than
 * the tests would gracefully stop
 *
 * Created by Andrea on 20/02/15.
 */
public class PrivateAccess {

    /**
     * flag to specify to throw AssumptionViolatedException when fields/methods do not
     * exist any more. this should bet set to false iff during experiments
     */
    private static boolean shouldNotFailTest = true;

    public static void setShouldNotFailTest(boolean b){
        shouldNotFailTest = b;
    }

    /**
     * Use reflection to set the given field
     *
     * @param klass
     * @param instance  null if field is static
     * @param fieldName
     * @param value
     * @param <T>  the class type
     * @throws IllegalArgumentException if klass or fieldName are null
     * @throws AssumptionViolatedException  if the the field does not exist anymore (eg due to refactoring)
     */
    public  static <T> void setVariable(Class<T> klass, T instance, String fieldName, Object value)
            throws IllegalArgumentException, AssumptionViolatedException {

        if(klass == null){
            throw new IllegalArgumentException("No specified class");
        }
        if(fieldName == null){
            throw new IllegalArgumentException("No specified field name");
        }
        // note: 'instance' can be null (ie, for static variables), and of course "value"

        Field field = null;
        try {
            field = klass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            String message = "Field '"+fieldName+"' does not exist any more in class "+klass;

            if(shouldNotFailTest) {
                // force the throwing of a JUnit AssumptionViolatedException
                throw new AssumptionViolatedException(message);
                //it is equivalent to calling
                //org.junit.Assume.assumeTrue(message,false);
            } else {
                throw new IllegalArgumentException(message);
            }
        }
        assert field != null;
        field.setAccessible(true);

        try {
            field.set(instance,value);
        } catch (IllegalAccessException e) {
            //should never happen, due to setAccessible(true);
            throw new AssumptionViolatedException("Failed to set field "+fieldName+": "+e.toString());
        }
    }


    /**
     * Use reflection to call the given method
     *
     * @param klass
     * @param instance  null for static methods
     * @param methodName
     * @param inputs  arrays of inputs
     * @param types   types for the inputs
     * @param <T>
     * @return the result of calling the method
     * @throws IllegalArgumentException if either klass or methodName are null
     * @throws AssumptionViolatedException  if method does not exist any more (eg, refactoring)
     * @throws Throwable the method might throw an internal exception
     */
    public static <T> Object callMethod(Class<T> klass, T instance, String methodName, Object[] inputs, Class<?>[] types)
            throws IllegalArgumentException, AssumptionViolatedException, Throwable {

        if(klass == null){
            throw new IllegalArgumentException("No specified class");
        }
        if(methodName == null){
            throw new IllegalArgumentException("No specified method name");
        }
        // note: 'instance' can be null (ie, for static methods), and of course "inputs"

        if( (types==null && inputs!=null) || (types!=null && inputs==null) ||(types!=null && inputs!=null && types.length!=inputs.length)){
            throw new IllegalArgumentException("Mismatch between input parameters and their type description");
        }

        Method method = null;
        try {
            method = klass.getDeclaredMethod(methodName,types);
        } catch (NoSuchMethodException e) {
            String message = "Method "+methodName+" does not exist anymore";
            if(shouldNotFailTest){
                throw new AssumptionViolatedException(message);
            } else {
                throw new IllegalArgumentException(message);
            }
        }
        assert method != null;
        method.setAccessible(true);

        Object result = null;

        try {
            result = method.invoke(instance,inputs);
        } catch (IllegalAccessException e) {
           //shouldn't really happen
            throw new AssumptionViolatedException("Failed to call "+methodName+": "+e.toString());
        } catch (InvocationTargetException e) {
            //we need to propagate the real cause to the test
            throw e.getTargetException();
        }

        return result;
    }

    /*
        TODO likely need one method per number of inputs
     */

    public static <T> Object callMethod(Class<T> klass, T instance, String methodName)
            throws IllegalArgumentException, AssumptionViolatedException, Throwable {
        return callMethod(klass,instance,methodName,new Object[0], new Class<?>[0]);
    }

    public static <T> Object callMethod(Class<T> klass, T instance, String methodName, Object input, Class<?> type)
            throws IllegalArgumentException, AssumptionViolatedException, Throwable {
        return callMethod(klass,instance,methodName,new Object[]{input}, new Class<?>[]{type});
    }


    public static Method getCallMethod(int nParameters){
        if(nParameters<0 || nParameters>10){ //TODO implement each of those methods
            return null;
        }

        List<Class<?>> types = new ArrayList<>();
        types.add(Class.class);//klass
        types.add(Object.class);//T
        types.add(String.class);//methodName

        for(int i=0; i<nParameters; i++){
            types.add(Object.class);
            types.add(Class.class);
        }

        try {
            return PrivateAccess.class.getDeclaredMethod("callMethod",types.toArray(new Class[0]));
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
