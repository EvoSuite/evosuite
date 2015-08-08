package org.evosuite.testcase.statements;


import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.GenericArrayType;
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
        String[] getStringArray(int[] input);
    }

    public static int base(Foo foo){
        return foo.getInt();
    }

    public static void all_once(Foo foo){
        foo.getBoolean();
        foo.getInt();
        foo.getDouble();
        foo.getString();
        foo.getLong();
        foo.getObject();
        foo.getStringArray(null);
    }

    public static void all_twice(Foo foo){
        all_once(foo);
        all_once(foo);
    }

    public static String getFirstInArray(Foo foo){
        int[] anArray = new int[]{123};
        String[] res = foo.getStringArray(anArray);
        if(res==null){
            return null;
        }
        return res[0];
    }

    @Test
    public void testAll_once()  throws Exception {
        TestCase tc = new DefaultTestCase();

        VariableReference ref = new VariableReferenceImpl(tc, Foo.class);
        FunctionalMockStatement mockStmt = new FunctionalMockStatement(tc, ref, Foo.class);
        VariableReference mock = tc.addStatement(mockStmt);
        VariableReference result = tc.addStatement(new MethodStatement(tc,
                new GenericMethod(this.getClass().getDeclaredMethod("all_once", Foo.class), FunctionalMockStatementTest.class),
                null, Arrays.asList(mock)));

        Assert.assertFalse(mockStmt.doesNeedToUpdateInputs());
        Assert.assertEquals(0, mockStmt.getNumParameters());

        //execute first time with default mock
        Scope scope = new Scope();
        for(Statement st : tc){
            st.execute(scope,System.out);
        }

        Assert.assertTrue(mockStmt.doesNeedToUpdateInputs());
        List<Type> types = mockStmt.updateMockedMethods();
        Assert.assertEquals(7,types.size());
    }

    @Test
    public void testAll_twice()  throws Exception {
        TestCase tc = new DefaultTestCase();

        VariableReference ref = new VariableReferenceImpl(tc, Foo.class);
        FunctionalMockStatement mockStmt = new FunctionalMockStatement(tc, ref, Foo.class);
        VariableReference mock = tc.addStatement(mockStmt);
        VariableReference result = tc.addStatement(new MethodStatement(tc,
                new GenericMethod(this.getClass().getDeclaredMethod("all_twice", Foo.class), FunctionalMockStatementTest.class),
                null, Arrays.asList(mock)));

        Assert.assertFalse(mockStmt.doesNeedToUpdateInputs());
        Assert.assertEquals(0, mockStmt.getNumParameters());

        //execute first time with default mock
        Scope scope = new Scope();
        for(Statement st : tc){
            st.execute(scope,System.out);
        }

        Assert.assertTrue(mockStmt.doesNeedToUpdateInputs());
        List<Type> types = mockStmt.updateMockedMethods();
        Assert.assertEquals(14,types.size());
    }


    @Test
    public void testArray() throws Exception{
        TestCase tc = new DefaultTestCase();

        /*
            String s = "...";
            String[] array = new String[1];
            array[0] = s;
            Foo foo = mock(Foo.class);
            getFirstInArray(foo);
         */

        final String MOCKED_VALUE = "Hello 42!!!";
        VariableReference aString  = tc.addStatement(new StringPrimitiveStatement(tc, MOCKED_VALUE));
        ArrayReference mockedArray = (ArrayReference) tc.addStatement(new ArrayStatement(tc,String[].class,1));
        ArrayIndex arrayIndex = new ArrayIndex(tc, mockedArray, 0);
        AssignmentStatement stmt = new AssignmentStatement(tc, arrayIndex, aString);
        tc.addStatement(stmt);

        FunctionalMockStatement mockStmt = new FunctionalMockStatement(tc, Foo.class, Foo.class);
        VariableReference mock = tc.addStatement(mockStmt);
        VariableReference result = tc.addStatement(new MethodStatement(tc,
                new GenericMethod(this.getClass().getDeclaredMethod("getFirstInArray", Foo.class), FunctionalMockStatementTest.class),
                null, Arrays.asList(mock)));


        //if not executed, should be no way to tell if needs new inputs
        Assert.assertFalse(mockStmt.doesNeedToUpdateInputs());
        Assert.assertEquals(0, mockStmt.getNumParameters());

        //execute first time with default mock
        Scope scope = new Scope();
        for(Statement st : tc){
            st.execute(scope,System.out);
        }

        Object obj =  scope.getObject(result);
        Assert.assertNull(obj); // default mock value should be null for objects/arrays


        //after execution, there should be one variable to provide
        Assert.assertTrue(mockStmt.doesNeedToUpdateInputs());
        List<Type> types = mockStmt.updateMockedMethods();
        Assert.assertEquals(1,types.size());
        Assert.assertEquals(String[].class, types.get(0));

        //add int variable to list of mock expected returns
        mockStmt.addMissingInputs(Arrays.asList(mockedArray));
        Assert.assertEquals(1, mockStmt.getNumParameters());
        Assert.assertTrue(mockStmt.getParameterReferences().get(0).same(mockedArray));

        //re-execute with initialized mock
        scope = new Scope();
        for(Statement st : tc){
            st.execute(scope,System.out);
        }

        String val = (String) scope.getObject(result);
        Assert.assertEquals(MOCKED_VALUE, val);
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