/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.testcase.statements;


import com.examples.with.different.packagename.fm.IssueWithNumber;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.instrumentation.NonInstrumentingClassLoader;
import org.evosuite.runtime.MockitoExtension;
import org.evosuite.runtime.RuntimeSettings;
import org.evosuite.runtime.instrumentation.EvoClassLoader;
import org.evosuite.runtime.instrumentation.RuntimeInstrumentation;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.execution.Scope;
import org.evosuite.testcase.statements.numeric.BooleanPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testcase.variable.VariableReferenceImpl;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Assert;
import org.junit.Test;
import sun.misc.ClassLoaderUtil;

import java.io.File;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Andrea Arcuri on 06/08/15.
 */
public class FunctionalMockStatementTest {

    private static final int DEFAULT_LIMIT = Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT;

    @After
    public void tearDown(){
        Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT = DEFAULT_LIMIT;
    }

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

    public static void limit(Foo foo, int x){
        for(int i=0; i<x ; i++){
            foo.getBoolean();
        }
    }

    private Scope execute(TestCase tc) throws Exception{
        Scope scope = new Scope();
        for(Statement st : tc){
            st.execute(scope,System.out);
        }
        return scope;
    }

    static class PackageLevel{
        PackageLevel(){}
    }

    public static class AClassWithPLMethod{

        String foo(){
            return "Value returned by package-level access method";
        }
    }

    public static class OverrideToString{
        @Override
        public String toString(){
            return "foo";
        }
    }

    public static abstract class OverrideToStringAbstract  implements java.io.Serializable {
        @Override
        public String toString(){
            return "foo";
        }

        public abstract double foo();

        public int bar(){return 1;}

        private static final long serialVersionUID = -8742448824652078965L;
    }

    //----------------------------------------------------------------------------------




    @Test
    public void testAClassWithPLMethod(){

        //FIXME once we support it
        assertFalse(FunctionalMockStatement.canBeFunctionalMocked(AClassWithPLMethod.class));
    }

    @Test
    public void testConfirmToString(){
        String res = new OverrideToString().toString();
        String diff = res + " a different string";

        OverrideToString obj = mock(OverrideToString.class);
        when(obj.toString()).thenReturn(diff);

        assertEquals(diff, obj.toString());
    }

    @Test
    public void testConfirmToStringAbstract(){

        String diff = " a different string";

        OverrideToStringAbstract obj = mock(OverrideToStringAbstract.class);
        when(obj.toString()).thenReturn(diff);

        assertEquals(diff, obj.toString());
    }

    @Test
    public void testConfirmNumber(){
        String foo = "foo";
        Number number = mock(Number.class);
        when(number.toString()).thenReturn(foo);

        assertEquals(foo, number.toString());
    }


    @Test
    public void testConfirmNumberExternalNoMockJVMNonDeterminism() throws Exception{
        RuntimeSettings.mockJVMNonDeterminism = false;
        testConfirmNumberExternal();
    }

    @Test
    public void testConfirmNumberExternalWithMockJVMNonDeterminism() throws Exception{
        RuntimeSettings.mockJVMNonDeterminism = true;
        testConfirmNumberExternal();
    }


    private void testConfirmNumberExternal() throws Exception{
        assertEquals(IssueWithNumber.RESULT, IssueWithNumber.getResult());

        RuntimeInstrumentation.setAvoidInstrumentingShadedClasses(true);


        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        EvoClassLoader loader = new EvoClassLoader();
        loader.skipInstrumentation(IssueWithNumber.class.getName());
        org.evosuite.runtime.Runtime.getInstance().resetRuntime();
        Class<?> klass = loader.loadClass(IssueWithNumber.class.getName());
        Method m = klass.getDeclaredMethod("getResult");
        String res = (String) m.invoke(null);

        assertEquals(IssueWithNumber.RESULT, res);
    }



    @Test
    public void testConfirmPackageLevel() throws Exception{

        Method m = AClassWithPLMethod.class.getDeclaredMethod("foo");
        assertFalse(Modifier.isPrivate(m.getModifiers()));
        assertFalse(Modifier.isPublic(m.getModifiers()));
        assertFalse(Modifier.isProtected(m.getModifiers()));
    }

    @Test
    public void testConfirmMockitoBehaviorOnPackageLevelAccess() throws Exception {

        //direct calls

        AClassWithPLMethod original = new AClassWithPLMethod();
        assertNotNull(original.foo());

        AClassWithPLMethod mocked = mock(AClassWithPLMethod.class);
        assertNull(mocked.foo());


        //reflection
        Method m = AClassWithPLMethod.class.getDeclaredMethod("foo");
        m.setAccessible(true);

        assertNotNull(m.invoke(original));
        assertNull(m.invoke(mocked));
    }


    @Test
    public void testConfirmCast(){

        //note: TypeUtils can give different results because it takes autoboxing into account

        assertTrue(TypeUtils.isAssignable(Integer.class, Integer.TYPE));
        assertTrue(TypeUtils.isAssignable(Integer.TYPE, Integer.class));
        assertFalse(Integer.TYPE.isAssignableFrom(Integer.class));
        assertFalse(Integer.class.isAssignableFrom(Integer.TYPE));


        assertFalse(Integer.TYPE.isAssignableFrom(Character.TYPE));
        assertFalse(TypeUtils.isAssignable(Integer.TYPE, Character.TYPE));

        assertFalse(Character.TYPE.isAssignableFrom(Integer.TYPE));
        assertTrue(TypeUtils.isAssignable(Character.TYPE, Integer.TYPE)); //DIFFERENT

        assertFalse(Character.class.isAssignableFrom(Integer.TYPE));
        assertTrue(TypeUtils.isAssignable(Character.class, Integer.TYPE)); //DIFFERENT

        assertFalse(Character.class.isAssignableFrom(Integer.class));
        assertFalse(TypeUtils.isAssignable(Character.class, Integer.class));

        assertTrue(Integer.TYPE.isPrimitive());
        assertFalse(Integer.class.isPrimitive());


        char c = 'c'; //99
        int i = c;

        assertEquals(99, i);

        Object aInt = i;
        Object aInteger = Integer.valueOf(7);

        Assert.assertTrue(aInt.getClass().equals(Integer.class));
        Assert.assertTrue(aInt.getClass().equals(aInteger.getClass()));

        Object aChar = c;
        Assert.assertTrue(aChar.getClass().equals(Character.class));

        //just recall the two diverge
        assertTrue(TypeUtils.isAssignable(aChar.getClass(), Integer.TYPE));
        assertFalse(Integer.TYPE.isAssignableFrom(aChar.getClass()));

        Object casted = null;
        try {
            casted = Integer.TYPE.cast(aChar);
            fail();
        } catch (Exception e){
            //expected: cannot do direct cast from "Character" to "int"
        }

        try {
            casted = Integer.TYPE.cast(((Character) aChar).charValue());
            fail();
        } catch (Exception e){
            //expected: "cast" takes an Object as input, so it does autoboxing :(
        }

        casted = (int) ((Character) aChar).charValue();

        assertTrue(casted.getClass().equals(Integer.class));
    }

    @Test
    public void testAvoidMockingEnvironment(){
        final boolean defaultValue = RuntimeSettings.useVFS;
        RuntimeSettings.useVFS = true;

        try {
            Assert.assertFalse(FunctionalMockStatement.canBeFunctionalMocked(File.class));
        } catch(Throwable t){
            RuntimeSettings.useVFS = defaultValue;
        }
    }


    @Test
    public void testPackageLevel_local()  throws Exception{
        TestCase tc = new DefaultTestCase();

        VariableReference ref = new VariableReferenceImpl(tc, PackageLevel.class);

        try {
            FunctionalMockStatement mockStmt = new FunctionalMockStatement(tc, ref, PackageLevel.class);
            fail();
        } catch (java.lang.IllegalArgumentException e){
            //expected
        }

        //tc.addStatement(mockStmt);
        //execute(tc);
    }


    @Test
    public void testPackageLevel_differentPackage()  throws Exception{
        TestCase tc = new DefaultTestCase();

        Class<?> example = Class.forName("com.examples.with.different.packagename.fm.ExamplePackageLevel");

        VariableReference ref = new VariableReferenceImpl(tc, example);

        try {
            FunctionalMockStatement mockStmt = new FunctionalMockStatement(tc, ref, example);
            fail();
        } catch (java.lang.IllegalArgumentException e){
            //expected
        }

        //tc.addStatement(mockStmt);
        //execute(tc);
    }

    @Test
    public void testPackageLevel_differentPackage_instrumentation_package()  throws Exception{
        TestCase tc = new DefaultTestCase();

        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        InstrumentingClassLoader loader = new InstrumentingClassLoader();
        Class<?> example = loader.loadClass("com.examples.with.different.packagename.fm.ExamplePackageLevel");

        VariableReference ref = new VariableReferenceImpl(tc, example);

        try {
            FunctionalMockStatement mockStmt = new FunctionalMockStatement(tc, ref, example);
            fail();
        } catch (java.lang.IllegalArgumentException e){
            //expected
        }

        //tc.addStatement(mockStmt);
        //execute(tc);
    }

    @Test
    public void testPackageLevel_differentPackage_nonInstrumentation_package()  throws Exception{
        TestCase tc = new DefaultTestCase();

        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        NonInstrumentingClassLoader loader = new NonInstrumentingClassLoader();
        Class<?> example = loader.loadClass("com.examples.with.different.packagename.fm.ExamplePackageLevel");

        VariableReference ref = new VariableReferenceImpl(tc, example);

        try {
            FunctionalMockStatement mockStmt = new FunctionalMockStatement(tc, ref, example);
            fail();
        } catch (java.lang.IllegalArgumentException e){
            //expected
        }

        //tc.addStatement(mockStmt);
        //execute(tc);
    }

    @Test
    public void testPackageLevel_differentPackage_instrumentation_public()  throws Exception{
        TestCase tc = new DefaultTestCase();

        ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
        InstrumentingClassLoader loader = new InstrumentingClassLoader();
        Class<?> example = loader.loadClass("com.examples.with.different.packagename.fm.ExamplePublicLevel");

        VariableReference ref = new VariableReferenceImpl(tc, example);
        FunctionalMockStatement mockStmt = new FunctionalMockStatement(tc, ref, example);

        tc.addStatement(mockStmt);
        execute(tc);
    }

    @Test
    public void testLimit() throws Exception{

        TestCase tc = new DefaultTestCase();

        final int LIMIT_5 = 5;
        Properties.FUNCTIONAL_MOCKING_INPUT_LIMIT = LIMIT_5;
        final int LOOP_0 = 0, LOOP_3 = 3 , LOOP_5 = 5, LOOP_7 = 7;


        IntPrimitiveStatement x = new IntPrimitiveStatement(tc, LOOP_3);
        VariableReference loop  = tc.addStatement(x);
        VariableReference boolRef = tc.addStatement(new BooleanPrimitiveStatement(tc,true));
        VariableReference ref = new VariableReferenceImpl(tc, Foo.class);
        FunctionalMockStatement mockStmt = new FunctionalMockStatement(tc, ref, Foo.class);
        VariableReference mock = tc.addStatement(mockStmt);
        tc.addStatement(new MethodStatement(tc,
                new GenericMethod(this.getClass().getDeclaredMethod("limit", Foo.class, int.class), FunctionalMockStatementTest.class),
                null, Arrays.asList(mock,loop)));

        //execute first time with default mock
        execute(tc);

        Assert.assertTrue(mockStmt.doesNeedToUpdateInputs());
        List<Type> types = mockStmt.updateMockedMethods();
        Assert.assertEquals(LOOP_3, types.size());
        for(Type t : types){
            Assert.assertEquals(boolean.class , t);
        }
        //add the 3 missing values
        mockStmt.addMissingInputs(Arrays.asList(boolRef, boolRef, boolRef));


        //before re-executing, change loops to the limit
        x.setValue(LOOP_5);
        execute(tc);

        Assert.assertTrue(mockStmt.doesNeedToUpdateInputs());
        types = mockStmt.updateMockedMethods();
        Assert.assertEquals(LOOP_5 - LOOP_3, types.size());
        for(Type t : types){
            Assert.assertEquals(boolean.class , t);
        }
        //add the 2 missing values
        mockStmt.addMissingInputs(Arrays.asList(boolRef, boolRef));
        Assert.assertEquals(LOOP_5, mockStmt.getNumParameters());


        //before re-executing 3rd time, change loops above the limit
        x.setValue(LOOP_7);
        execute(tc);

        Assert.assertFalse(mockStmt.doesNeedToUpdateInputs()); //no update should be required
        types = mockStmt.updateMockedMethods();
        Assert.assertEquals(0, types.size());
        Assert.assertEquals(LOOP_5, mockStmt.getNumParameters());


        //decrease, but to the limit, so still no required change
        x.setValue(LOOP_5);
        execute(tc);

        Assert.assertFalse(mockStmt.doesNeedToUpdateInputs()); //no update should be required
        types = mockStmt.updateMockedMethods();
        Assert.assertEquals(0, types.size());
        Assert.assertEquals(LOOP_5, mockStmt.getNumParameters());

        //further decrease, but now we need to remove parameters
        x.setValue(LOOP_3);
        execute(tc);

        Assert.assertTrue(mockStmt.doesNeedToUpdateInputs()); //do update
        types = mockStmt.updateMockedMethods();
        Assert.assertEquals(0, types.size()); // but no new types to add
        Assert.assertEquals(LOOP_3, mockStmt.getNumParameters());

        //remove all
        x.setValue(LOOP_0);
        execute(tc);

        Assert.assertTrue(mockStmt.doesNeedToUpdateInputs()); //do update
        types = mockStmt.updateMockedMethods();
        Assert.assertEquals(0, types.size()); // but no new types to add
        Assert.assertEquals(LOOP_0, mockStmt.getNumParameters());
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
        Scope scope = execute(tc);

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
        Scope scope = execute(tc);

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
        Scope scope = execute(tc);

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
        scope = execute(tc);

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
        Scope scope = execute(tc);

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