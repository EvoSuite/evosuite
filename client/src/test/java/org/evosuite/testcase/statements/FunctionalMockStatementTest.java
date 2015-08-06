package org.evosuite.testcase.statements;


import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Andrea Arcuri on 06/08/15.
 */
public class FunctionalMockStatementTest {

    public interface Foo{
        boolean getBoolean();
        int getInt();
        double getDouble();
        String getString();
        long getLong();
        Object getObject();
    }

    public static int base(Foo foo){
        return foo.getInt();
    }


    @Test
    public void testBase() throws Exception {
        TestCase tc = new DefaultTestCase();


        final int MOCKED_VALUE = 42;
        VariableReference mockedInput  = tc.addStatement(new IntPrimitiveStatement(tc, MOCKED_VALUE));
        VariableReference ref = new VariableReferenceImpl(tc, Foo.class);
        FunctionalMockStatement mockStmt = new FunctionalMockStatement(tc, ref, Foo.class);
        VariableReference mock = tc.addStatement(mockStmt);
        VariableReference result = tc.addStatement(new MethodStatement(tc,
                new GenericMethod(this.getClass().getDeclaredMethod("base", Foo.class), FunctionalMockStatementTest.class),
                null, Arrays.asList(mock)));


        //if not executed, should be no way to tell if needs new inputs
        Assert.assertFalse(mockStmt.doesNeedToUpdateInputs());
        Assert.assertEquals(0, mockStmt.getNumParameters());

        //execute first time with default mock
        Scope scope = new Scope();
        for(Statement st : tc){
            st.execute(scope,System.out);
        }

        Integer val = (Integer) scope.getObject(result);
        Assert.assertEquals(0 , val.intValue()); // default mock value should be 0



        //after execution, there should be one variable to provide
        Assert.assertTrue(mockStmt.doesNeedToUpdateInputs());
        List<Type> types = mockStmt.updateMockedMethods();
        Assert.assertEquals(1,types.size());
        Assert.assertEquals(int.class, types.get(0));

        //add int variable to list of mock expected returns
        mockStmt.addMissingInputs(Arrays.asList(mockedInput));
        Assert.assertEquals(1, mockStmt.getNumParameters());
        Assert.assertTrue(mockStmt.getParameterReferences().get(0).same(mockedInput));

        //re-execute with initialized mock
        scope = new Scope();
        for(Statement st : tc){
            st.execute(scope,System.out);
        }

        val = (Integer) scope.getObject(result);
        Assert.assertEquals(MOCKED_VALUE, val.intValue());
    }

}