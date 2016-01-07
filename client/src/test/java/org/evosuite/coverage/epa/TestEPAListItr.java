package org.evosuite.coverage.epa;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Method;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assume;
import org.junit.Test;

import com.examples.with.different.packagename.epa.ListItr;
import com.examples.with.different.packagename.epa.MyArrayList;

public class TestEPAListItr extends TestEPATransitionCoverage {

	@Test
	public void testSingleTrace() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		Properties.TARGET_CLASS = ListItr.class.getName();

		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		Class<?> listItrClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(Properties.TARGET_CLASS);
		Class<?> arrayListClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MyArrayList.class.getName());

		// adds "MyArrayList list = new MyArrayList();"
		VariableReference arrayListVar = builder.addConstructorStatement(arrayListClass);

		// adds "ListItr itr = list.listIterator();"
		Method m = arrayListClass.getMethod("listIterator");
		VariableReference itrVar = builder.addMethodStatement(arrayListVar, m);

		// adds "Object object0 = new Object();"
		VariableReference object0 = builder.addConstructorStatement(Object.class);

		// adds "itr.add(object0);"
		Method addMethod = listItrClass.getMethod("add", Object.class);
		builder.addMethodStatement(itrVar, addMethod, object0);

		DefaultTestCase tc = builder.toTestCase();
		// Expected Trace
		// S0->ListItr()->S1->add()->S3
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
