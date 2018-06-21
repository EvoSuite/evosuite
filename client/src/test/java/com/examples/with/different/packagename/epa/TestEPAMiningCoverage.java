package com.examples.with.different.packagename.epa;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.epa.EPAMiningCoverageSuiteFitness;
import org.evosuite.coverage.epa.EPATestCaseBuilder;
import org.evosuite.coverage.epa.TestEPACoverage;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Test;

public class TestEPAMiningCoverage extends TestEPACoverage {

	@Override
	protected Criterion[] getCriteria() {
		return new Criterion[] { Criterion.EPAMINING };
	}

	@Test
	public void testNewPushPushPopPop() throws NoSuchMethodException, SecurityException, ClassNotFoundException {

		Properties.TEST_ARCHIVE = false;
		Properties.TIMEOUT = Integer.MAX_VALUE;
		Properties.TARGET_CLASS = BoundedStackSize3ForMining.class.getName();

		DefaultTestCase tc = buildNewPushPushPopPopTestCase();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPAMiningCoverageSuiteFitness epaMiningFitness = new EPAMiningCoverageSuiteFitness();

		suite.addFitness(epaMiningFitness);
		double fitnessValue = epaMiningFitness.getFitness(suite);

		int numberOfActions = 3;
		int numberOfStates = (int) Math.pow(2, 3);
		int expectedMaxTransitions = numberOfStates * numberOfActions * numberOfStates;

		int expectedCoveredTransitions = 5;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedMaxTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitnessValue = suite.getFitness();
		assertEquals(fitnessValue, suiteFitnessValue, 0.00000001);
	}

	@Test
	public void testNewPush() throws NoSuchMethodException, SecurityException, ClassNotFoundException {

		Properties.TEST_ARCHIVE = false;
		Properties.TIMEOUT = Integer.MAX_VALUE;
		Properties.TARGET_CLASS = BoundedStackSize3ForMining.class.getName();

		DefaultTestCase tc = buildNewPushTestCase();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPAMiningCoverageSuiteFitness epaMiningFitness = new EPAMiningCoverageSuiteFitness();

		suite.addFitness(epaMiningFitness);
		double fitnessValue = epaMiningFitness.getFitness(suite);

		int numberOfActions = 3;
		int numberOfStates = (int) Math.pow(2, 3);
		int expectedMaxTransitions = numberOfStates * numberOfActions * numberOfStates;

		int expectedCoveredTransitions = 2;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedMaxTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitnessValue = suite.getFitness();
		assertEquals(fitnessValue, suiteFitnessValue, 0.00000001);
	}

	@Test
	public void testNewPop() throws NoSuchMethodException, SecurityException, ClassNotFoundException {

		Properties.TEST_ARCHIVE = false;
		Properties.TIMEOUT = Integer.MAX_VALUE;
		Properties.TARGET_CLASS = BoundedStackSize3ForMining.class.getName();

		DefaultTestCase tc = buildNewPopTestCase();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPAMiningCoverageSuiteFitness epaMiningFitness = new EPAMiningCoverageSuiteFitness();

		suite.addFitness(epaMiningFitness);
		double fitnessValue = epaMiningFitness.getFitness(suite);

		int numberOfActions = 3;
		int numberOfStates = (int) Math.pow(2, 3);
		int expectedMaxTransitions = numberOfStates * numberOfActions * numberOfStates;

		int expectedCoveredTransitions = 1;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedMaxTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitnessValue = suite.getFitness();
		assertEquals(fitnessValue, suiteFitnessValue, 0.00000001);
	}

	
	@Test
	public void testNewPushPopInvalidState() throws NoSuchMethodException, SecurityException, ClassNotFoundException {

		Properties.TEST_ARCHIVE = false;
		Properties.TIMEOUT = Integer.MAX_VALUE;
		Properties.TARGET_CLASS = BoundedStackSize3ForMiningWithInvalidState.class.getName();

		DefaultTestCase tc = buildNewPushPopInvalidStateTestCase();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPAMiningCoverageSuiteFitness epaMiningFitness = new EPAMiningCoverageSuiteFitness();

		suite.addFitness(epaMiningFitness);
		double fitnessValue = epaMiningFitness.getFitness(suite);

		int numberOfActions = 3;
		int numberOfStates = (int) Math.pow(2, 3);
		int expectedMaxTransitions = numberOfStates * numberOfActions * numberOfStates;

		int expectedCoveredTransitions = 2;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedMaxTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitnessValue = suite.getFitness();
		assertEquals(fitnessValue, suiteFitnessValue, 0.00000001);
	}

	@Test
	public void testNew() throws NoSuchMethodException, SecurityException, ClassNotFoundException {

		Properties.TEST_ARCHIVE = false;
		Properties.TIMEOUT = Integer.MAX_VALUE;
		Properties.TARGET_CLASS = BoundedStackSize3ForMining.class.getName();

		DefaultTestCase tc = buildNewTestCase();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPAMiningCoverageSuiteFitness epaMiningFitness = new EPAMiningCoverageSuiteFitness();

		suite.addFitness(epaMiningFitness);
		double fitnessValue = epaMiningFitness.getFitness(suite);

		int numberOfActions = 3;
		int numberOfStates = (int) Math.pow(2, 3);
		int expectedMaxTransitions = numberOfStates * numberOfActions * numberOfStates;

		int expectedCoveredTransitions = 1;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedMaxTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitnessValue = suite.getFitness();
		assertEquals(fitnessValue, suiteFitnessValue, 0.00000001);
	}

	@Test
	public void testNewPushPushPushPopPopPop() throws NoSuchMethodException, SecurityException, ClassNotFoundException {

		Properties.TEST_ARCHIVE = false;
		Properties.TIMEOUT = Integer.MAX_VALUE;
		Properties.TARGET_CLASS = BoundedStackSize3ForMining.class.getName();

		DefaultTestCase tc = buildNewPushPushPushPopPopPopTestCase();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPAMiningCoverageSuiteFitness epaMiningFitness = new EPAMiningCoverageSuiteFitness();

		suite.addFitness(epaMiningFitness);
		double fitnessValue = epaMiningFitness.getFitness(suite);

		int numberOfActions = 3;
		int numberOfStates = (int) Math.pow(2, 3);
		int expectedMaxTransitions = numberOfStates * numberOfActions * numberOfStates;

		int expectedCoveredTransitions = 7;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedMaxTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitnessValue = suite.getFitness();
		assertEquals(fitnessValue, suiteFitnessValue, 0.00000001);
	}

	private DefaultTestCase buildNewTestCase() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		Constructor<?> boundedStackConstructor = clazz.getConstructor();

		// adds "MyBoundedStack stack = new MyBoundedStack();"
		builder.addConstructorStatement(boundedStackConstructor);

		DefaultTestCase tc = builder.toTestCase();
		return tc;
	}

	private DefaultTestCase buildNewPushTestCase()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		Constructor<?> boundedStackConstructor = clazz.getConstructor();
		Method pushMethod = clazz.getMethod("push", int.class);

		// adds "int int0 = 0;"
		VariableReference int0 = builder.addIntegerStatement(0);
		// adds "MyBoundedStack stack = new MyBoundedStack();"
		VariableReference stackVar = builder.addConstructorStatement(boundedStackConstructor);
		// adds "stack.push(object0);"
		builder.addMethodStatement(stackVar, pushMethod, int0);

		DefaultTestCase tc = builder.toTestCase();
		return tc;
	}
	
	private DefaultTestCase buildNewPushPopInvalidStateTestCase()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		Constructor<?> boundedStackConstructor = clazz.getConstructor();
		Method pushMethod = clazz.getMethod("push", int.class);
		Method popMethod = clazz.getMethod("pop");

		// adds "int int0 = 0;"
		VariableReference int0 = builder.addIntegerStatement(0);
		// adds "MyBoundedStack stack = new MyBoundedStack();"
		VariableReference stackVar = builder.addConstructorStatement(boundedStackConstructor);
		// adds "stack.push(object0);"
		builder.addMethodStatement(stackVar, pushMethod, int0);
		// adds "stack.pop();"
		builder.addMethodStatement(stackVar, popMethod);

		DefaultTestCase tc = builder.toTestCase();
		return tc;
	}

	private DefaultTestCase buildNewPopTestCase()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		Constructor<?> boundedStackConstructor = clazz.getConstructor();
		Method popMethod = clazz.getMethod("pop");

		// adds "MyBoundedStack stack = new MyBoundedStack();"
		VariableReference stackVar = builder.addConstructorStatement(boundedStackConstructor);
		// adds "stack.push(object0);"
		builder.addMethodStatement(stackVar, popMethod);

		DefaultTestCase tc = builder.toTestCase();
		return tc;
	}

	private DefaultTestCase buildNewPushPushPopPopTestCase()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		Constructor<?> boundedStackConstructor = clazz.getConstructor();
		Method pushMethod = clazz.getMethod("push", int.class);
		Method popMethod = clazz.getMethod("pop");

		// adds "int int0 = 0;"
		VariableReference int0 = builder.addIntegerStatement(0);
		// adds "MyBoundedStack stack = new MyBoundedStack();"
		VariableReference stackVar = builder.addConstructorStatement(boundedStackConstructor);
		// adds "stack.push(object0);"
		builder.addMethodStatement(stackVar, pushMethod, int0);
		// adds "stack.push(object0);"
		builder.addMethodStatement(stackVar, pushMethod, int0);
		// adds "object1 = stack.pop();"
		builder.addMethodStatement(stackVar, popMethod);
		// adds "object1 = stack.pop();"
		builder.addMethodStatement(stackVar, popMethod);

		DefaultTestCase tc = builder.toTestCase();
		return tc;
	}

	private DefaultTestCase buildNewPushPushPushPopPopPopTestCase()
			throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		Constructor<?> boundedStackConstructor = clazz.getConstructor();
		Method pushMethod = clazz.getMethod("push", int.class);
		Method popMethod = clazz.getMethod("pop");

		// adds "int int0 = 0;"
		VariableReference int0 = builder.addIntegerStatement(0);
		// adds "MyBoundedStack stack = new MyBoundedStack();"
		VariableReference stackVar = builder.addConstructorStatement(boundedStackConstructor);
		// adds "stack.push(object0);"
		builder.addMethodStatement(stackVar, pushMethod, int0);
		// adds "stack.push(object0);"
		builder.addMethodStatement(stackVar, pushMethod, int0);
		// adds "stack.push(object0);"
		builder.addMethodStatement(stackVar, pushMethod, int0);
		// adds "object1 = stack.pop();"
		builder.addMethodStatement(stackVar, popMethod);
		// adds "object1 = stack.pop();"
		builder.addMethodStatement(stackVar, popMethod);
		// adds "object1 = stack.pop();"
		builder.addMethodStatement(stackVar, popMethod);

		DefaultTestCase tc = builder.toTestCase();
		return tc;
	}
}
