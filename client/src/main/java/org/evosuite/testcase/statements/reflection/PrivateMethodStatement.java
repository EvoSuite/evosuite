package org.evosuite.testcase.statements.reflection;

import org.evosuite.runtime.PrivateAccess;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.FieldReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericField;
import org.evosuite.utils.GenericMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andrea Arcuri on 22/02/15.
 */
public class PrivateMethodStatement extends MethodStatement {

    public PrivateMethodStatement(TestCase tc, Class<?> klass , String methodName, VariableReference callee, List<VariableReference> params)
            throws NoSuchFieldException {
        super(
                tc,
                new GenericMethod(getPrivateAccessMethod(params.size()),PrivateAccess.class),
                null, //it is static
                getReflectionParams(tc,klass,methodName,callee,params)
        );
    }

    private static List<VariableReference> getReflectionParams(TestCase tc, Class<?> klass , String methodName,
                                                               VariableReference callee, List<VariableReference> inputs)
            throws NoSuchFieldException{

        List<VariableReference> list = new ArrayList<>(3 + inputs.size()*2);
        list.add(new FieldReference(tc, new GenericField(klass.getDeclaredField("class"), klass)));
        list.add(callee);
        list.add(new ConstantValue(tc, new GenericClass(String.class), methodName));

        for(VariableReference vr : inputs){
            list.add(vr);
            list.add(new FieldReference(tc, new GenericField(vr.getVariableClass().getDeclaredField("class"), vr.getVariableClass())));
        }

        return list;
    }

    private static Method getPrivateAccessMethod(int n){
        return null; //TODO
    }


    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        return super.mutate(test,factory); //TODO
    }
}
