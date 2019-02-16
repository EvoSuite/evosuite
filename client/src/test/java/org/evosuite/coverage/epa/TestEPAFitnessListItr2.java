package org.evosuite.coverage.epa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.examples.with.different.packagename.epa.ListItr;
import com.examples.with.different.packagename.epa.MyArrayList;

public class TestEPAFitnessListItr2 extends TestEPATransitionCoverage {

	@Before
	public void prepareTest() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());
		Properties.EPA_XML_PATH = xmlFilename;
		
	}

	@After
	public void tearDownTest() {
		Properties.EPA_XML_PATH = null;
	}

	@Test
	public void testSingleTrace() throws NoSuchMethodException, SecurityException, ClassNotFoundException,
			ParserConfigurationException, SAXException, IOException, MalformedEPATraceException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);

		Properties.TARGET_CLASS = ListItr.class.getName();

		DefaultTestCase tc = buildTestCase0();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPATransitionCoverageSuiteFitness fitness = new EPATransitionCoverageSuiteFitness(xmlFilename);

		suite.addFitness(fitness);
		double fitnessValue = fitness.getFitness(suite);

		// There are 69 transitions in ListItr's EPA
		int expectedTotalTransitions = 69;

		// There are only 2 transitions in the test case
		int expectedCoveredTransitions = 2;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitness = suite.getFitness();
		assertTrue(suiteFitness == fitnessValue);

		EPATransitionCoverageFactory factory = new EPATransitionCoverageFactory(Properties.TARGET_CLASS, EPAFactory.buildEPA(xmlFilename));
		List<EPATransitionCoverageTestFitness> goals = factory.getCoverageGoals();
		assertEquals(69, goals.size());

		TestChromosome testChromosome = suite.getTestChromosome(0);
		ExecutionResult execResult = testChromosome.executeForFitnessFunction(fitness);

		List<EPATrace> epaTraces = new LinkedList<EPATrace>(execResult.getTrace().getEPATraces());
		assertTrue(!epaTraces.isEmpty());

		int covered = 0;
		int uncovered = 0;
		double totalFitness = 0.0;
		for (EPATransitionCoverageTestFitness g : goals) {
			double fitnessPerGoal = g.getFitness(testChromosome);
			if (fitnessPerGoal == 0.0) {
				covered++;
			} else {
				uncovered++;
			}
			totalFitness += fitnessPerGoal;
		}

		assertEquals(69, covered + uncovered);
		assertEquals(2, covered);
		assertEquals(67, uncovered);

		double coverage = (double) 2 / (double) 69;
		assertEquals(coverage, suite.getCoverage(), 0.000001);
	}

	private static DefaultTestCase buildTestCase0() throws ClassNotFoundException, NoSuchMethodException {
		Class<?> listItrClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(ListItr.class.getName());
		Class<?> arrayListClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MyArrayList.class.getName());

		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		// adds "MyArrayList list = new MyArrayList();"
		Constructor<?> arrayListCtor = arrayListClass.getConstructor();
		VariableReference arrayListVar = builder.addConstructorStatement(arrayListCtor);

		// adds "ListItr itr = list.listIterator();"
		Method m = arrayListClass.getMethod("listIterator");
		VariableReference itrVar = builder.addMethodStatement(arrayListVar, m);

		// adds "Object object0 = new Object();"
		Constructor<Object> objectConstructor = Object.class.getConstructor();
		VariableReference object0 = builder.addConstructorStatement(objectConstructor);

		// adds "itr.add(object0);"
		Method addMethod = listItrClass.getMethod("add", Object.class);
		builder.addMethodStatement(itrVar, addMethod, object0);

		DefaultTestCase tc = builder.toTestCase();
		return tc;
	}
}
