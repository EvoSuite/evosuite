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

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assume;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.examples.with.different.packagename.epa.ListItr;
import com.examples.with.different.packagename.epa.MyArrayList;

public class TestEPAFitnessListItr extends TestEPATransitionCoverage {

	@Test
	public void testSingleTrace() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		Properties.TARGET_CLASS = ListItr.class.getName();

		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		DefaultTestCase tc = buildTestCase(builder);

		// Expected Trace
		// S0->ListItr()->S1->add()->S3
		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPATransitionCoverageSuiteFitness fitness = new EPATransitionCoverageSuiteFitness(xmlFilename);

		suite.addFitness(fitness);
		double fitnessValue = fitness.getFitness(suite);

		// There are 37 transitions in ListItr's EPA
		int expectedTotalTransitions = 69;

		// There are only 2 transitions in the test case
		int expectedCoveredTransitions = 2;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);
		
		double suiteFitness = suite.getFitness();
		assertTrue(suiteFitness==fitnessValue);

	}

	private static DefaultTestCase buildTestCase(EPATestCaseBuilder builder)
			throws ClassNotFoundException, NoSuchMethodException {
		Class<?> listItrClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(Properties.TARGET_CLASS);
		Class<?> arrayListClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MyArrayList.class.getName());

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

	@Test
	public void testEPAStates() throws ClassNotFoundException, NoSuchMethodException, FileNotFoundException,
			ParserConfigurationException, SAXException, IOException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		EPA epa = EPAFactory.buildEPA(xmlFilename);

		Properties.TARGET_CLASS = ListItr.class.getName();

		EPATestCaseBuilder builder = new EPATestCaseBuilder();

		DefaultTestCase tc = buildTestCase(builder);

		// Expected Trace
		// S0->ListItr()->S1->add()->S3
		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPATransitionCoverageSuiteFitness fitness = new EPATransitionCoverageSuiteFitness(xmlFilename);

		List<ExecutionResult> results = new LinkedList<ExecutionResult>();
		for (ExecutableChromosome chromosome : suite.getTestChromosomes()) {
			ExecutionResult result = chromosome.executeForFitnessFunction(fitness);
			results.add(result);
		}
		assertEquals(1, results.size());

		ExecutionResult executionResult = results.get(0);
		List<EPATrace> traces = EPATraceFactory.buildEPATraces(Properties.TARGET_CLASS, executionResult.getTrace(),
				epa);

		assertEquals(1, traces.size());

		EPATrace trace = traces.get(0);
		assertEquals(2, trace.getEpaTransitions().size());

		EPATransition t1 = trace.getEpaTransitions().get(0);
		EPATransition t2 = trace.getEpaTransitions().get(1);

		EPAState sinit = new EPAState("Sinit");
		EPAState s87 = new EPAState("S87");
		EPAState s119 = new EPAState("S119");

		assertEquals(sinit, t1.getOriginState());
		assertEquals("<init>(Lcom/examples/with/different/packagename/epa/MyArrayList;I)V", t1.getActionName());
		assertEquals(s87, t1.getDestinationState());

		assertEquals(s87, t2.getOriginState());
		assertEquals("add(Ljava/lang/Object;)V", t2.getActionName());
		assertEquals(s119, t2.getDestinationState());

	}

	@Test
	public void testFitness() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());
	
		Properties.TARGET_CLASS = ListItr.class.getName();
	
		EPATestCaseBuilder builder = new EPATestCaseBuilder();
	
		DefaultTestCase tc = buildTestCase(builder);
	
		// Expected Trace
		// S0->ListItr()->S1->add()->S3
		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);
	
		EPATransitionCoverageSuiteFitness epaTransitionFitness = new EPATransitionCoverageSuiteFitness(xmlFilename);
	
		suite.addFitness(epaTransitionFitness);
		double fitnessValue = epaTransitionFitness.getFitness(suite);
	
		
		// There are 37 transitions in ListItr's EPA
		int expectedTotalTransitions = 69;
	
		// There are only 2 transitions in the test case
		int expectedCoveredTransitions = 2;
	
		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;
	
		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);
		
		double suiteFitnessValue = suite.getFitness();
		assertEquals(fitnessValue, suiteFitnessValue, 0.00000001);
	}
}
