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
package org.evosuite.junit.naming.methods;

import static org.evosuite.coverage.io.IOCoverageConstants.ARRAY_EMPTY;
import static org.evosuite.coverage.io.IOCoverageConstants.ARRAY_NONEMPTY;
import static org.evosuite.coverage.io.IOCoverageConstants.NUM_NEGATIVE;
import static org.evosuite.coverage.io.IOCoverageConstants.NUM_POSITIVE;
import static org.evosuite.coverage.io.IOCoverageConstants.NUM_ZERO;
import static org.evosuite.coverage.io.IOCoverageConstants.REF_NONNULL;
import static org.evosuite.coverage.io.IOCoverageConstants.REF_NULL;
import static org.junit.Assert.assertEquals;

import org.evosuite.coverage.exception.ExceptionCoverageTestFitness;
import org.evosuite.coverage.io.input.InputCoverageGoal;
import org.evosuite.coverage.io.input.InputCoverageTestFitness;
import org.evosuite.coverage.io.output.OutputCoverageGoal;
import org.evosuite.coverage.io.output.OutputCoverageTestFitness;
import org.evosuite.coverage.method.MethodCoverageTestFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFitnessFunction;
import org.evosuite.testcase.statements.numeric.IntPrimitiveStatement;
import org.junit.Test;
import org.objectweb.asm.Type;

import java.util.ArrayList;

/**
 * Created by jmr on 31/07/15.
 *
 * TODO: These tests need more work, this is currently rather a mess.
 *
 */
public class TestMethodNamingComplexExamples {

	@Test
	public void testTwoTestsOutputGoals() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {
		TestFitnessFunction goal1 = new MethodCoverageTestFitness("FooClass","keys()[I");
		TestFitnessFunction goal2 = new MethodCoverageTestFitness("FooClass", "<init>(LFooClass;)V");

		TestFitnessFunction goal3 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "keys()",Type.INT_TYPE, NUM_NEGATIVE));
		TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "keys()",Type.INT_TYPE, NUM_POSITIVE));

		DefaultTestCase test1 = new DefaultTestCase();
		test1.addCoveredGoal(goal1);
		test1.addCoveredGoal(goal2);
		test1.addCoveredGoal(goal3);

		DefaultTestCase test2 = new DefaultTestCase();
		test2.addStatement(new IntPrimitiveStatement(test2, 2)); // Need to add statements to change hashCode
		test2.addCoveredGoal(goal1);
		test2.addCoveredGoal(goal2);
		test2.addCoveredGoal(goal4);

		ArrayList<TestCase> testCases = new ArrayList<>();
		testCases.add(test1);
		testCases.add(test2);

		CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
		String nameTest1 = naming.getName(test1);
		String nameTest2 = naming.getName(test2);
		assertEquals("Generated test name differs from expected", "testKeysTakingNoArgumentsReturningNegative", nameTest1); // TODO: testKeysReturningNegative?
		assertEquals("Generated test name differs from expected", "testKeysTakingNoArgumentsReturningPositive", nameTest2); // TODO: testKeysReturningPositive?
	}

	@Test
	public void testThreeTestsOutputGoals() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

		// Method goals
		TestFitnessFunction goal1 = new MethodCoverageTestFitness("FooClass", "mist(I)V");
		TestFitnessFunction goal2 = new MethodCoverageTestFitness("FooClass", "keys()");

		// Output goals for method keys()I
		TestFitnessFunction goal3 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "keys()", Type.INT_TYPE, NUM_POSITIVE));
		TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "keys()", Type.INT_TYPE, NUM_NEGATIVE));

		DefaultTestCase test1 = new DefaultTestCase();
		test1.addCoveredGoal(goal1);

		DefaultTestCase test2 = new DefaultTestCase();
		test2.addStatement(new IntPrimitiveStatement(test2, 2)); // Need to add statements to change hashCode
		test2.addCoveredGoal(goal1);
		test2.addCoveredGoal(goal2);
		test2.addCoveredGoal(goal3);

		DefaultTestCase test3 = new DefaultTestCase();
		test3.addStatement(new IntPrimitiveStatement(test3, 3)); // Need to add statements to change hashCode
		test3.addCoveredGoal(goal1);
		test3.addCoveredGoal(goal2);
		test3.addCoveredGoal(goal4);

		ArrayList<TestCase> testCases = new ArrayList<>();
		testCases.add(test1);
		testCases.add(test2);
		testCases.add(test3);

		CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
		String nameTest1 = naming.getName(test1);
		String nameTest2 = naming.getName(test2);
		String nameTest3 = naming.getName(test3);
		assertEquals("Generated test name differs from expected", "testMist", nameTest1);
		assertEquals("Generated test name differs from expected", "testKeysReturningPositive", nameTest2);
		assertEquals("Generated test name differs from expected", "testKeysReturningNegative", nameTest3);
	}

	@Test
	public void testOverloadedMethods() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

		// Method goals
		TestFitnessFunction goal1 = new MethodCoverageTestFitness("FooClass", "<init>(LFooClass;)V");
		TestFitnessFunction goal2 = new MethodCoverageTestFitness("FooClass","values([B)[B");
		TestFitnessFunction goal3 = new MethodCoverageTestFitness("FooClass","values([I)[B");

		// Output goals for method values
		TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "values([B)",Type.getType("[B"), ARRAY_EMPTY));
		TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "values([I)",Type.getType("[B"),ARRAY_NONEMPTY));

		DefaultTestCase test1 = new DefaultTestCase();
		test1.addCoveredGoal(goal1);
		test1.addCoveredGoal(goal2);
		test1.addCoveredGoal(goal4);

		DefaultTestCase test2 = new DefaultTestCase();
		test2.addStatement(new IntPrimitiveStatement(test2, 2)); // Need to add statements to change hashCode
		test2.addCoveredGoal(goal1);
		test2.addCoveredGoal(goal3);
		test2.addCoveredGoal(goal5);

		ArrayList<TestCase> testCases = new ArrayList<>();
		testCases.add(test1);
		testCases.add(test2);

		CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
		String nameTest1 = naming.getName(test1);
		String nameTest2 = naming.getName(test2);
		assertEquals("Generated test name differs from expected", "testValuesTakingByteArray", nameTest1);
		assertEquals("Generated test name differs from expected", "testValuesTakingIntArray", nameTest2);
	}

	@Test
	public void testOverloadedMethodsNoArgs() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

		// Method goals
		TestFitnessFunction goal1 = new MethodCoverageTestFitness("FooClass", "<init>(LFooClass;)V");
		TestFitnessFunction goal2 = new MethodCoverageTestFitness("FooClass","values([B)[B");
		TestFitnessFunction goal3 = new MethodCoverageTestFitness("FooClass","values()[B");

		// Output goals for method values
		TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "values([B)",Type.getType("[B"), ARRAY_EMPTY));
		TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "values()",Type.getType("[B"), ARRAY_NONEMPTY));

		DefaultTestCase test1 = new DefaultTestCase();
		test1.addCoveredGoal(goal1);
		test1.addCoveredGoal(goal2);
		test1.addCoveredGoal(goal4);

		DefaultTestCase test2 = new DefaultTestCase();
		test2.addStatement(new IntPrimitiveStatement(test2, 2)); // Need to add statements to change hashCode
		test2.addCoveredGoal(goal1);
		test2.addCoveredGoal(goal3);
		test2.addCoveredGoal(goal5);

		ArrayList<TestCase> testCases = new ArrayList<>();
		testCases.add(test1);
		testCases.add(test2);

		CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
		String nameTest1 = naming.getName(test1);
		String nameTest2 = naming.getName(test2);
		assertEquals("Generated test name differs from expected", "testValuesTakingByteArray", nameTest1);
		assertEquals("Generated test name differs from expected", "testValuesTakingNoArguments", nameTest2);
	}


	@Test
	public void testConstructors() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

		// method goal
		TestFitnessFunction goal1 = new MethodCoverageTestFitness("FooClass", "<init>(LFooClass;)V");

		// exception goal
		TestFitnessFunction goal2 = new ExceptionCoverageTestFitness("FooClass", "<init>(LFooClass;)V", ArrayIndexOutOfBoundsException.class, ExceptionCoverageTestFitness.ExceptionType.IMPLICIT);

		DefaultTestCase test1 = new DefaultTestCase();
		test1.addCoveredGoal(goal1);

		DefaultTestCase test2 = new DefaultTestCase();
		test2.addStatement(new IntPrimitiveStatement(test2, 2)); // Need to add statements to change hashCode
		test2.addCoveredGoal(goal1);
		test2.addCoveredGoal(goal2);

		ArrayList<TestCase> testCases = new ArrayList<>();
		testCases.add(test1);
		testCases.add(test2);

		CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
		String nameTest1 = naming.getName(test1);
		String nameTest2 = naming.getName(test2);
		assertEquals("Generated test name differs from expected", "testCreatesFooClass", nameTest1);
		assertEquals("Generated test name differs from expected", "testFailsToCreateFooClassThrowsArrayIndexOutOfBoundsException", nameTest2);
	}


	@Test
	public void testExactSameTests() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

		// method goals
		TestFitnessFunction goal1 = new MethodCoverageTestFitness("FooClass","values([B)[B");
		TestFitnessFunction goal2 = new MethodCoverageTestFitness("FooClass","values()[B");

		// output goals
		TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "values([B)",Type.getType("[B"), ARRAY_EMPTY));
		TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "values()",Type.getType("[B"), ARRAY_NONEMPTY));

		DefaultTestCase test1 = new DefaultTestCase();
		test1.addCoveredGoal(goal1);
		test1.addCoveredGoal(goal2);
		test1.addCoveredGoal(goal4);
		test1.addCoveredGoal(goal5);

		DefaultTestCase test2 = new DefaultTestCase();
		test2.addStatement(new IntPrimitiveStatement(test2, 2)); // Need to add statements to change hashCode
		test2.addCoveredGoal(goal1);
		test2.addCoveredGoal(goal2);
		test2.addCoveredGoal(goal4);
		test2.addCoveredGoal(goal5);

		ArrayList<TestCase> testCases = new ArrayList<>();
		testCases.add(test1);
		testCases.add(test2);

		CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
		String nameTest1 = naming.getName(test1);
		String nameTest2 = naming.getName(test2);
		assertEquals("Generated test name differs from expected", "testValuesTakingNoArgumentsReturningNonEmptyArray", nameTest1);
		assertEquals("Generated test name differs from expected", "testValuesTakingByteArrayReturningEmptyArray", nameTest2);
	}

	@Test
	public void testConstructorAndOverloadedMethods() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

		// method goal
		TestFitnessFunction goal1 = new MethodCoverageTestFitness("FooClass", "<init>(LFooClass;)V");
		TestFitnessFunction goal2 = new MethodCoverageTestFitness("FooClass", "values()[B");
		TestFitnessFunction goal3 = new MethodCoverageTestFitness("FooClass", "values([B)[B");

		// exception goal
		TestFitnessFunction goal4 = new ExceptionCoverageTestFitness("FooClass", "<init>(LFooClass;)V", ArrayIndexOutOfBoundsException.class ,ExceptionCoverageTestFitness.ExceptionType.IMPLICIT);

		// output goals
		TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "values()",Type.getType("[B"), ARRAY_NONEMPTY));
		TestFitnessFunction goal6 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "values([B)",Type.getType("[B"), ARRAY_EMPTY));

		DefaultTestCase test1 = new DefaultTestCase();
		test1.addCoveredGoal(goal1);
		test1.addCoveredGoal(goal2);
		test1.addCoveredGoal(goal3);
		test1.addCoveredGoal(goal5);
		test1.addCoveredGoal(goal6);

		DefaultTestCase test2 = new DefaultTestCase();
		test2.addStatement(new IntPrimitiveStatement(test2, 2)); // Need to add statements to change hashCode
		test2.addCoveredGoal(goal1);
		test2.addCoveredGoal(goal2);
		test2.addCoveredGoal(goal3);
		test2.addCoveredGoal(goal4);
		test2.addCoveredGoal(goal5);
		test2.addCoveredGoal(goal6);

		ArrayList<TestCase> testCases = new ArrayList<>();
		testCases.add(test1);
		testCases.add(test2);

		CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
		String nameTest1 = naming.getName(test1);
		String nameTest2 = naming.getName(test2);
		assertEquals("Generated test name differs from expected", "testValuesTakingNoArgumentsReturningNonEmptyArray", nameTest1);
		assertEquals("Generated test name differs from expected", "testFailsToCreateFooClassThrowsArrayIndexOutOfBoundsException", nameTest2);
	}

	@Test
	public void testIDNamingWithSameMethodGoals() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

		// method goals
		TestFitnessFunction goal1 = new MethodCoverageTestFitness("FooClass","<init>()V");
		TestFitnessFunction goal2 = new MethodCoverageTestFitness("FooClass","getPublicID()Ljava/lang/String;");
		TestFitnessFunction goal3 = new MethodCoverageTestFitness("FooClass","setPublicID(Ljava/lang/String;)LFooClass;");

		// output goals
		TestFitnessFunction goal4 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "getPublicID()",Type.getType("Ljava.lang.String;"), REF_NONNULL));
		TestFitnessFunction goal5 = new OutputCoverageTestFitness(new OutputCoverageGoal("FooClass", "setPublicID(Ljava/lang/String;)",Type.getType("LFooClass;"), REF_NONNULL));

		DefaultTestCase test1 = new DefaultTestCase();
		test1.addStatement(new IntPrimitiveStatement(test1, 1)); // any statement to fool hashcode function

		test1.addCoveredGoal(goal1);
		test1.addCoveredGoal(goal2);
		test1.addCoveredGoal(goal4);

		DefaultTestCase test2 = new DefaultTestCase();
		test2.addStatement(new IntPrimitiveStatement(test2, 2)); // any statement to fool hashcode function
		test2.addCoveredGoal(goal1);
		test2.addCoveredGoal(goal3);
		test2.addCoveredGoal(goal5);

		ArrayList<TestCase> testCases = new ArrayList<>();
		testCases.add(test1);
		testCases.add(test2);

		CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
		String nameTest1 = naming.getName(test1);
		String nameTest2 = naming.getName(test2);
		assertEquals("Generated test name differs from expected", "testGetPublicIDTakingNoArguments", nameTest1); // TODO: testGetPublicID ?
		assertEquals("Generated test name differs from expected", "testSetPublicIDTakingString", nameTest2); // TODO: testSetPublicID ?
	}

	@Test
	public void testIDNamingWithSameMethodGoals2() throws NoSuchMethodException, ConstructionFailedException, ClassNotFoundException {

		// method goals
		TestFitnessFunction goal1 = new MethodCoverageTestFitness("FooClass","<init>(LField;II)V");
		TestFitnessFunction goal2 = new MethodCoverageTestFitness("FooClass","<init>(LFieldMatrix;)V");

		// exception goals
		TestFitnessFunction goal3 = new ExceptionCoverageTestFitness("FooClass", "<init>(LField;II)V", ArrayIndexOutOfBoundsException.class ,ExceptionCoverageTestFitness.ExceptionType.IMPLICIT);

		// input goals
		TestFitnessFunction goal4 = new InputCoverageTestFitness(new InputCoverageGoal("FooClass", "<init>(LFieldMatrix;)V", 0, Type.getType("LFieldMatrix;"), REF_NULL));
		TestFitnessFunction goal5 = new InputCoverageTestFitness(new InputCoverageGoal("FooClass", "<init>(LFieldMatrix;)V", 0, Type.getType("LFieldMatrix;"), REF_NONNULL));

		TestFitnessFunction goal6 = new InputCoverageTestFitness(new InputCoverageGoal("FooClass", "<init>(LField;II)V", 0, Type.getType("LField;"), REF_NONNULL));
		TestFitnessFunction goal7 = new InputCoverageTestFitness(new InputCoverageGoal("FooClass", "<init>(LField;II)V", 1, Type.INT_TYPE, NUM_ZERO));
		TestFitnessFunction goal8 = new InputCoverageTestFitness(new InputCoverageGoal("FooClass", "<init>(LField;II)V", 1, Type.INT_TYPE, NUM_NEGATIVE));
		TestFitnessFunction goal9 = new InputCoverageTestFitness(new InputCoverageGoal("FooClass", "<init>(LField;II)V", 2, Type.INT_TYPE, NUM_ZERO));

		DefaultTestCase test1 = new DefaultTestCase();
		test1.addCoveredGoal(goal1);
		test1.addCoveredGoal(goal3);
		test1.addCoveredGoal(goal6);
		test1.addCoveredGoal(goal7); // unique
		test1.addCoveredGoal(goal9); // unique

		DefaultTestCase test2 = new DefaultTestCase();
		test2.addStatement(new IntPrimitiveStatement(test2, 2)); // Need to add statements to change hashCode
		test2.addCoveredGoal(goal2);
		test2.addCoveredGoal(goal3);
		test2.addCoveredGoal(goal4); // unique

		DefaultTestCase test3 = new DefaultTestCase();
		test3.addStatement(new IntPrimitiveStatement(test3, 3)); // Need to add statements to change hashCode
		test3.addCoveredGoal(goal1);
		test3.addCoveredGoal(goal2);
		test3.addCoveredGoal(goal3);
		test3.addCoveredGoal(goal5); // unique
		test3.addCoveredGoal(goal6);
		test3.addCoveredGoal(goal8); // unique

		ArrayList<TestCase> testCases = new ArrayList<>();
		testCases.add(test1);
		testCases.add(test2);
		testCases.add(test3);

		CoverageGoalTestNameGenerationStrategy naming = new CoverageGoalTestNameGenerationStrategy(testCases);
		String nameTest1 = naming.getName(test1);
		String nameTest2 = naming.getName(test2);
		String nameTest3 = naming.getName(test3);
		assertEquals("Generated test name differs from expected", "testCreatesFooClassTaking3ArgumentsWithZeroAndZero", nameTest1);
		assertEquals("Generated test name differs from expected", "testCreatesFooClassTakingFieldMatrixWithNull", nameTest2);
		// TODO: Why is Positive vs nonnull nondeterministically chosen?
		assertEquals("Generated test name differs from expected", "testCreatesFooClassTaking3ArgumentsWithNegative", nameTest3);
	}
}
