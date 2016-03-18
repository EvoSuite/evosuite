package org.evosuite.coverage.epa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.examples.with.different.packagename.epa.MyBoundedStack;

public class TestEPAFitnessMyBoundedStack extends TestEPATransitionCoverage {

	private static final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
			"resources", "epas", "MyBoundedStack.xml");
	private Set<ExecutionObserver> previous_observers = null;

	@Before
	public void prepareTest() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		Properties.EPA_XML_PATH = xmlFilename;
		EPA automata = EPAFactory.buildEPA(xmlFilename);
		previous_observers = TestCaseExecutor.getInstance().getExecutionObservers();
		TestCaseExecutor.getInstance().newObservers();
		TestCaseExecutor.getInstance().addObserver(new EPATraceObserver(automata));
	}

	@After
	public void tearDownTest() {
		Properties.EPA_XML_PATH = null;
		if (previous_observers!=null) {
			TestCaseExecutor.getInstance().setExecutionObservers(previous_observers);
		}

	}

	@Test
	public void testSingleTrace() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		Properties.TARGET_CLASS = MyBoundedStack.class.getName();

		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);

		// adds "MyBoundedStack stack = new MyBoundedStack();"
		Constructor<?> constructor = clazz.getConstructor();
		VariableReference stackVar = builder.addConstructorStatement(constructor);

		// adds "Object object0 = new Object();"
		Constructor<Object> objectConstructor = Object.class.getConstructor();
		VariableReference object0 = builder.addConstructorStatement(objectConstructor);

		// adds "stack.push(object0);"
		Method pushMethod = clazz.getMethod("push", Object.class);
		builder.addMethodStatement(stackVar, pushMethod, object0);

		// adds "object1 = stack.pop();"
		Method popMethod = clazz.getMethod("pop");
		builder.addMethodStatement(stackVar, popMethod);

		DefaultTestCase tc = builder.toTestCase();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPATransitionCoverageSuiteFitness fitness = new EPATransitionCoverageSuiteFitness(xmlFilename);

		suite.addFitness(fitness);
		double fitnessValue = fitness.getFitness(suite);

		int expectedTotalTransitions = 7;
		int expectedCoveredTransitions = 3;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitness = suite.getFitness();
		assertTrue(suiteFitness == fitnessValue);

	}

	@Test
	public void testLongerSingleTrace() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		Properties.TARGET_CLASS = MyBoundedStack.class.getName();

		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);

		Constructor<?> constructor = clazz.getConstructor();
		Method pushMethod = clazz.getMethod("push", Object.class);
		Method popMethod = clazz.getMethod("pop");

		Constructor<Object> objectConstructor = Object.class.getConstructor();
		VariableReference object0 = builder.addConstructorStatement(objectConstructor);

		VariableReference stackVar = builder.addConstructorStatement(constructor);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, popMethod);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, popMethod);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, pushMethod, object0);
		builder.addMethodStatement(stackVar, popMethod);

		DefaultTestCase tc = builder.toTestCase();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPATransitionCoverageSuiteFitness fitness = new EPATransitionCoverageSuiteFitness(xmlFilename);

		suite.addFitness(fitness);
		double fitnessValue = fitness.getFitness(suite);

		// There are 7 transitions in ListItr's EPA
		int expectedTotalTransitions = 7;

		// There are only 2 transitions in the test case
		int expectedCoveredTransitions = 7;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitness = suite.getFitness();
		assertTrue(suiteFitness == fitnessValue);
	}

	@Test
	public void testException() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		Properties.TARGET_CLASS = MyBoundedStack.class.getName();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		Constructor<?> constructor = clazz.getConstructor();
		Method popMethod = clazz.getMethod("pop");

		EPATestCaseBuilder builder = new EPATestCaseBuilder();
		VariableReference stackVar = builder.addConstructorStatement(constructor);
		builder.addMethodStatement(stackVar, popMethod);

		DefaultTestCase tc = builder.toTestCase();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPATransitionCoverageSuiteFitness fitness = new EPATransitionCoverageSuiteFitness(xmlFilename);

		suite.addFitness(fitness);
		double fitnessValue = fitness.getFitness(suite);

		// There are 7 transitions in ListItr's EPA
		int expectedTotalTransitions = 7;

		// There are only 1 transitions in the test case
		int expectedCoveredTransitions = 1;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitness = suite.getFitness();
		assertTrue(suiteFitness == fitnessValue);
	}

	@Test
	public void testOnlyConstructor() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		Properties.TARGET_CLASS = MyBoundedStack.class.getName();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		Constructor<?> constructor = clazz.getConstructor();

		EPATestCaseBuilder builder = new EPATestCaseBuilder();
		builder.addConstructorStatement(constructor);

		DefaultTestCase tc = builder.toTestCase();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPATransitionCoverageSuiteFitness fitness = new EPATransitionCoverageSuiteFitness(xmlFilename);

		suite.addFitness(fitness);
		double fitnessValue = fitness.getFitness(suite);

		// There are 7 transitions in ListItr's EPA
		int expectedTotalTransitions = 7;

		// There are only 2 transitions in the test case
		int expectedCoveredTransitions = 1;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitness = suite.getFitness();
		assertTrue(suiteFitness == fitnessValue);

	}

	@Test
	public void testTwoStacks() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		Properties.TARGET_CLASS = MyBoundedStack.class.getName();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		Constructor<?> constructor = clazz.getConstructor();
		Method pushMethod = clazz.getMethod("push", Object.class);
		Constructor<Object> objectConstructor = Object.class.getConstructor();

		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		VariableReference stack0 = builder.addConstructorStatement(constructor);
		VariableReference stack1 = builder.addConstructorStatement(constructor);
		VariableReference object0 = builder.addConstructorStatement(objectConstructor);
		builder.addMethodStatement(stack0, pushMethod, object0);
		builder.addMethodStatement(stack1, pushMethod, object0);

		DefaultTestCase tc = builder.toTestCase();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPATransitionCoverageSuiteFitness fitness = new EPATransitionCoverageSuiteFitness(xmlFilename);

		suite.addFitness(fitness);
		double fitnessValue = fitness.getFitness(suite);

		// There are 7 transitions in ListItr's EPA
		int expectedTotalTransitions = 7;

		// There are only 2 transitions in the test case
		int expectedCoveredTransitions = 2;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitness = suite.getFitness();
		assertTrue(fitnessValue == suiteFitness);
	}

}
