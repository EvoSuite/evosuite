package org.evosuite.testcase.statements.reflection;

import org.evosuite.runtime.PrivateAccess;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.statements.ClassPrimitiveStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.variable.ConstantValue;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Test case statement representing a reflection call to a private method of the SUT
 *
 * Created by Andrea Arcuri on 22/02/15.
 */
public class PrivateMethodStatement extends MethodStatement {

	private static final long serialVersionUID = -4555899888145880432L;

	public PrivateMethodStatement(TestCase tc, Class<?> klass , String methodName, VariableReference callee, List<VariableReference> params)
            throws NoSuchFieldException {
        super(
                tc,
                new GenericMethod(PrivateAccess.getCallMethod(params.size()),PrivateAccess.class),
                null, //it is static
                getReflectionParams(tc,klass,methodName,callee,params)
        );
        List<GenericClass> parameterTypes = new ArrayList<GenericClass>();
        parameterTypes.add(new GenericClass(klass));
        this.method.setTypeParameters(parameterTypes);
    }

    private static List<VariableReference> getReflectionParams(TestCase tc, Class<?> klass , String methodName,
                                                               VariableReference callee, List<VariableReference> inputs)
            throws NoSuchFieldException{

        List<VariableReference> list = new ArrayList<>(3 + inputs.size()*2);
        list.add(new ConstantValue(tc,new GenericClass(Class.class),klass));
        list.add(callee);
        list.add(new ConstantValue(tc, new GenericClass(String.class), methodName));

        for(VariableReference vr : inputs){
            list.add(vr);
            list.add(new ConstantValue(tc,new GenericClass(Class.class),vr.getVariableClass()));
        }

        return list;
    }

    @Override
    public boolean mutate(TestCase test, TestFactory factory) {
        // just for simplicity
        return false;
        //return super.mutate(test,factory); //tricky, as should do some restrictions
    }
}
