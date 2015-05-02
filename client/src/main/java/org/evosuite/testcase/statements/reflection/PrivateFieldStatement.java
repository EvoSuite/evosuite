package org.evosuite.testcase.statements.reflection;

import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.runtime.PrivateAccess;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Statement representing the setting of a private field, which is done through reflection in the
 * generated JUnit tests.
 *
 * Created by foo on 20/02/15.
 */
public class PrivateFieldStatement extends MethodStatement{

	private static final long serialVersionUID = 5152490398872348493L;

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
            throws NoSuchFieldException, IllegalArgumentException, ConstructionFailedException {
        super(
                tc,
                new GenericMethod(setVariable,PrivateAccess.class),
                null, //it is static
                Arrays.asList(  // setVariable(Class<T> klass, T instance, String fieldName, Object value)
                        new ConstantValue(tc,new GenericClass(Class.class),klass),  // Class<T> klass
                        //new ClassPrimitiveStatement(tc,klass).getReturnValue(),  // Class<T> klass
                        callee, // T instance
                        new ConstantValue(tc,new GenericClass(String.class),fieldName),  // String fieldName
                        param // Object value
                )
        );
        List<GenericClass> parameterTypes = new ArrayList<GenericClass>();
        parameterTypes.add(new GenericClass(klass));
        this.method.setTypeParameters(parameterTypes);
    }

    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        // just for simplicity
        return false;
        //return super.mutate(test,factory); //tricky, as should do some restrictions
    }
}
