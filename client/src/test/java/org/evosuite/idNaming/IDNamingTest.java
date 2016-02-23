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
package org.evosuite.idNaming;

import com.examples.with.different.packagename.NullString;
import com.examples.with.different.packagename.junit.Foo;
import org.evosuite.Properties;
import org.evosuite.assertion.Assertion;
import org.evosuite.assertion.PrimitiveAssertion;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.junit.writer.TestSuiteWriter;
import org.evosuite.testcase.ConstantInliner;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestCodeVisitor;
import org.evosuite.testcase.statements.ArrayStatement;
import org.evosuite.testcase.statements.AssignmentStatement;
import org.evosuite.testcase.statements.ConstructorStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testcase.statements.NullStatement;
import org.evosuite.testcase.statements.numeric.BooleanPrimitiveStatement;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.evosuite.testcase.variable.ArrayIndex;
import org.evosuite.testcase.variable.ArrayReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.generic.GenericConstructor;
import org.evosuite.utils.generic.GenericMethod;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Jose Rojas
 *
 */
public class IDNamingTest {

    @Test
    public void testSimpleVariableNamesWithDummyStrategy() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
	    Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.DUMMY;
	    testWithTwoVariables(new String[]{"var0", "var1"});
    }

	@Test
	public void testSimpleVariableNamesWithDefaultStrategy() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
		Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.DEFAULT;
		testWithTwoVariables(new String[]{"foo", "int0"});
	}

	@Test
	public void testSimpleVariableNamesWithExplanatoryStrategy() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
		Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.EXPLANATORY;
		testWithTwoVariables(new String[]{"invokesAdd", "resultFromAdd"});
	}

	@Test
	public void testSimpleVariableNamesWithNaturalizeStrategy() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
		Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.NATURALIZE;
		Properties.VARIABLE_NAMING_TRAINING_DATA_DIR = null; // should set this to something
		// because there's no training data, we expect default names
		testWithTwoVariables(new String[]{"foo", "int0"});
	}

	private void testWithTwoVariables(String[] expectedNames) throws NoSuchMethodException, ClassNotFoundException {
        // test
        DefaultTestCase tc = createTestCaseWithPrimitiveStatements();

        // check variable names
        VariableReference var0 = tc.getStatement(0).getReturnValue();
        VariableReference var1 = tc.getStatement(1).getReturnValue();

        TestCodeVisitor tcv = new TestCodeVisitor();
        tcv.initializeNamingStrategyFromProperties();
	    tc.accept(tcv);

        System.out.println(tcv.getCode()); // Force execution of strategy.finalize()
        Assert.assertEquals("Unexpected variable name", expectedNames[0], tcv.getVariableName(var0));
        Assert.assertEquals("Unexpected variable name", expectedNames[1], tcv.getVariableName(var1));

    }

    private DefaultTestCase createTestCaseWithPrimitiveStatements() throws NoSuchMethodException, ClassNotFoundException {
        Class<?> sut = Foo.class;
        DefaultTestCase tc = new DefaultTestCase();

        // int int0 = 5;
        VariableReference int0 = tc.addStatement(new IntPrimitiveStatement(tc, 5));
        // int int1 = 3;
        VariableReference int1 = tc.addStatement(new IntPrimitiveStatement(tc, 3));

        // Foo foo0 = new Foo();
        GenericConstructor fooConstructor = new GenericConstructor(sut.getConstructors()[0], sut);
        ConstructorStatement fooConstructorStatement = new ConstructorStatement(tc, fooConstructor, Arrays.asList(new VariableReference[] {}));
        VariableReference foo0 = tc.addStatement(fooConstructorStatement);

        // int int2 = foo0.add(int0,int1);
        Method fooIncMethod = sut.getMethod("add", new Class<?>[] { Integer.TYPE, Integer.TYPE});
        GenericMethod incMethod = new GenericMethod(fooIncMethod, sut);
        VariableReference int2 = tc.addStatement(new MethodStatement(tc, incMethod, foo0, Arrays.asList(new VariableReference[] {int0, int1})));

        // foo0.add(int0,int2);
        MethodStatement methodStmt = new MethodStatement(tc, incMethod, foo0, Arrays.asList(new VariableReference[]{int0, int2}));
        attachAssertion(methodStmt, sut, int2, 8);
        tc.addStatement(methodStmt);

        new ConstantInliner().inline(tc);
        return tc;
    }

    private void attachAssertion(MethodStatement methodStmt, Class<?> sut, VariableReference source, Object value) {
        Mutation killedMutant = new Mutation(sut.getCanonicalName(), "add(II)I", "operator", 0, new BytecodeInstruction(null,sut.getCanonicalName(),"add(II)I",2,0,new InsnNode(0)), (InsnList)null, null);
        Assertion assertion = new PrimitiveAssertion();
        assertion.setStatement(methodStmt);
        assertion.setSource(source);
        assertion.setValue(value);
        assertion.addKilledMutation(killedMutant);
        methodStmt.addAssertion(assertion);
    }

    private void writeTestSuite(TestCase tc, String suiteName) {
        TestSuiteWriter tsw = new TestSuiteWriter();
        tsw.insertTest(tc);
        tsw.writeTestSuite(suiteName, Properties.TEST_DIR);
    }

    @Test
    public void testVariableNamesArray() throws NoSuchMethodException {
        DefaultTestCase tc = new DefaultTestCase();

        ArrayStatement as = new ArrayStatement(tc, String[].class, 2);
        tc.addStatement(as);

        ArrayReference arrayVar = as.getArrayReference();

        ArrayIndex ai0 = new ArrayIndex(tc, arrayVar, 0);
        ArrayIndex ai1 = new ArrayIndex(tc, arrayVar, 1);

        ConstructorStatement cs = new ConstructorStatement(tc, new GenericConstructor(String.class.getConstructor(), String.class), new ArrayList<>());
        VariableReference objectVar = tc.addStatement(cs);
        tc.addStatement(new AssignmentStatement(tc, ai0, objectVar));

        NullStatement nullStmt = new NullStatement(tc, String.class);
        tc.addStatement(nullStmt);
        tc.addStatement(new AssignmentStatement(tc, ai1, nullStmt.getReturnValue()));

        IntPrimitiveStatement intStmt = new IntPrimitiveStatement(tc, 42);
        tc.addStatement(intStmt);

        ConstructorStatement sutCS = new ConstructorStatement(tc, new GenericConstructor(NullString.class.getConstructor(), NullString.class), new ArrayList<VariableReference>());
        VariableReference sut = tc.addStatement(sutCS);

        List<VariableReference> parameters = new ArrayList<>();
        parameters.add(ai0);
        tc.addStatement(new MethodStatement(tc, new GenericMethod(NullString.class.getMethods()[0], NullString.class), sut, parameters));
        parameters = new ArrayList<>();
        parameters.add(ai1);
        tc.addStatement(new MethodStatement(tc, new GenericMethod(NullString.class.getMethods()[0], NullString.class), sut, parameters));
        parameters = new ArrayList<>();
        parameters.add(intStmt.getReturnValue());
        VariableReference call = tc.addStatement(new MethodStatement(tc, new GenericMethod(NullString.class.getMethods()[0], NullString.class), sut, parameters));

        BooleanPrimitiveStatement boolStmt = new BooleanPrimitiveStatement(tc, true);
        tc.addStatement(boolStmt);
        tc.addStatement(new AssignmentStatement(tc, boolStmt.getReturnValue(), call));

        (new ConstantInliner()).inline(tc);

        //Visit the tests
        Properties.VARIABLE_NAMING_STRATEGY = Properties.VariableNamingStrategy.EXPLANATORY;
        TestCodeVisitor tcv = new TestCodeVisitor();
        tcv.initializeNamingStrategyFromProperties();
        tcv.visitTestCase(tc);

        Assert.assertEquals("Unexpected variable name", "stringArray"   , tcv.getVariableName(arrayVar));
        Assert.assertEquals("Unexpected variable name", "newString"     , tcv.getVariableName(objectVar));
        Assert.assertEquals("Unexpected variable name", "invokesIsNull" , tcv.getVariableName(sut));
        Assert.assertEquals("Unexpected variable name", "stringArray[1]", tcv.getVariableName(ai1));

        //writeTestSuite(tc, "FooExplanatoryArrayTest");
    }

    @Test
    public void testCandidatesComparison() {

        // Create the sorted set
        SortedSet set = new TreeSet();
        ExplanatoryNamingTestVisitor tv = new ExplanatoryNamingTestVisitor(null);

        // Add elements to the set
        set.add(tv.new CandidateName(ExplanatoryNamingTestVisitor.CandidateSource.ASSERTION  , "v1", 10));
        set.add(tv.new CandidateName(ExplanatoryNamingTestVisitor.CandidateSource.PARAMETER  , "v3", 1));
        set.add(tv.new CandidateName(ExplanatoryNamingTestVisitor.CandidateSource.CONSTRUCTOR, "v4", 5));
        set.add(tv.new CandidateName(ExplanatoryNamingTestVisitor.CandidateSource.PARAMETER  , "v2", 10));
        set.add(tv.new CandidateName(ExplanatoryNamingTestVisitor.CandidateSource.CONSTRUCTOR, "v5", 2));

        Assert.assertEquals(((ExplanatoryNamingTestVisitor.CandidateName)set.first()).getName(), "v2");
    }

}
