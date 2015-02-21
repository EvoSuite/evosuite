package org.evosuite.testcase.statements.reflection;

import org.evosuite.runtime.PrivateAccess;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericField;
import org.evosuite.utils.GenericMethod;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Statement representing the setting of a private field, which is done with through reflection in the
 * generated JUnit tests.
 *
 * Created by foo on 20/02/15.
 */
public class PrivateFieldStatement extends MethodStatement{

    private static Method setVariable;

    static {
        try {
            //Class<T> klass, T instance, String fieldName, Object value
            setVariable = PrivateAccess.class.getMethod("setVariable",Class.class, Object.class,String.class,Object.class);
        } catch (NoSuchMethodException e) {
            //should never happen
            throw new RuntimeException("EvoSuite bug",e);
        }
    }

    public PrivateFieldStatement(TestCase tc, Class<?> klass , String fieldName, VariableReference callee, VariableReference param)
            throws NoSuchFieldException {
        super(
                tc,
                new GenericMethod(setVariable,PrivateAccess.class),
                null, //it is static
                Arrays.asList(  // setVariable(Class<T> klass, T instance, String fieldName, Object value)
                        new FieldReference(tc,new GenericField(klass.getDeclaredField("class"),klass)),  // Class<T> klass
                        callee, // T instance
                        new ConstantValue(tc,new GenericClass(String.class),fieldName),  // String fieldName
                        param // Object value
                )
        );
    }
}
