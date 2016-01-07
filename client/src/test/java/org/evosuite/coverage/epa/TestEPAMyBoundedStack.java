package org.evosuite.coverage.epa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assume;
import org.junit.Test;

import com.examples.with.different.packagename.epa.MyBoundedStack;

public class TestEPAMyBoundedStack extends TestEPATransitionCoverage {

	@Test
	public void testSingleTrace() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "MyBoundedStack.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		Properties.TARGET_CLASS = MyBoundedStack.class.getName();

		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(Properties.TARGET_CLASS);

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

		// There are 37 transitions in ListItr's EPA
		int expectedTotalTransitions = 37;

		// There are only 2 transitions in the test case
		int expectedCoveredTransitions = 2;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);
	}
}
