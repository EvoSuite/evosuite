package org.evosuite.coverage.epa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.testcase.DefaultTestCase;
import org.evosuite.testcase.ExecutableChromosome;
import org.evosuite.testcase.execution.ExecutionObserver;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTrace;
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

public class TestEPAFitnessListItr extends TestEPATransitionCoverage {

	private int DEFAULT_TIMEOUT;

	@Test
	public void testSingleTrace() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

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

	@Test
	public void testEPAStates() throws ClassNotFoundException, NoSuchMethodException, FileNotFoundException,
			ParserConfigurationException, SAXException, IOException, MalformedEPATraceException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		Properties.TARGET_CLASS = ListItr.class.getName();

		DefaultTestCase tc = buildTestCase0();

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
		List<EPATrace> traces = new LinkedList<EPATrace>(executionResult.getTrace().getEPATraces());

		assertEquals(1, traces.size());

		EPATrace trace = traces.get(0);
		assertEquals(2, trace.getEpaTransitions().size());

		EPATransition t1 = trace.getEpaTransitions().get(0);
		EPATransition t2 = trace.getEpaTransitions().get(1);

		EPAState sinit = new EPAState("Sinit");
		EPAState s87 = new EPAState("S87");
		EPAState s119 = new EPAState("S119");

		assertEquals(sinit, t1.getOriginState());
		assertEquals("ListItr()", t1.getActionName());
		assertEquals(s87, t1.getDestinationState());

		assertEquals(s87, t2.getOriginState());
		assertEquals("add()", t2.getActionName());
		assertEquals(s119, t2.getDestinationState());

	}

	@Test
	public void testFitness() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		Properties.EPA_XML_PATH = xmlFilename;
		Properties.TARGET_CLASS = ListItr.class.getName();

		DefaultTestCase tc = buildTestCase0();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPATransitionCoverageSuiteFitness epaTransitionFitness = new EPATransitionCoverageSuiteFitness(xmlFilename);

		suite.addFitness(epaTransitionFitness);
		double fitnessValue = epaTransitionFitness.getFitness(suite);

		int expectedTotalTransitions = 69;
		int expectedCoveredTransitions = 2;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitnessValue = suite.getFitness();
		assertEquals(fitnessValue, suiteFitnessValue, 0.00000001);
	}

	@Test
	public void testExceptionOnConstructor() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		Properties.TARGET_CLASS = ListItr.class.getName();

		DefaultTestCase tc = buildTestCase1();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPATransitionCoverageSuiteFitness epaTransitionFitness = new EPATransitionCoverageSuiteFitness(xmlFilename);

		suite.addFitness(epaTransitionFitness);
		double fitnessValue = epaTransitionFitness.getFitness(suite);

		int expectedTotalTransitions = 69;
		int expectedCoveredTransitions = 0;

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;

		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitnessValue = suite.getFitness();
		assertEquals(fitnessValue, suiteFitnessValue, 0.00000001);

	}

	private static DefaultTestCase buildTestCase1() throws ClassNotFoundException, NoSuchMethodException {
		EPATestCaseBuilder builder = new EPATestCaseBuilder();
		Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(ListItr.class.getName());
		Class<?> arrayListClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MyArrayList.class.getName());
		VariableReference nullArrayList = builder.addNullStatement(arrayListClass);
		VariableReference int0 = builder.addIntegerStatement(0);
		Constructor<?> constructor = clazz.getConstructor(arrayListClass, int.class);
		builder.addConstructorStatement(constructor, nullArrayList, int0);
		DefaultTestCase tc = builder.toTestCase();
		return tc;
	}

	private static DefaultTestCase buildTestCase2() throws ClassNotFoundException, NoSuchMethodException {
		EPATestCaseBuilder builder = new EPATestCaseBuilder();
		Class<?> arrayListClass = TestGenerationContext.getInstance().getClassLoaderForSUT()
				.loadClass(MyArrayList.class.getName());

		Constructor<?> constructor = arrayListClass.getConstructor();
		VariableReference arrayList0Var = builder.addConstructorStatement(constructor);
		VariableReference arrayList1Var = builder.addConstructorStatement(constructor);

		Method addMethod = arrayListClass.getMethod("add", Object.class);
		builder.addMethodStatement(arrayList0Var, addMethod, arrayList1Var);
		builder.addMethodStatement(arrayList1Var, addMethod, arrayList0Var);

		Method retainAllMehtod = arrayListClass.getMethod("retainAll", Collection.class);
		builder.addMethodStatement(arrayList1Var, retainAllMehtod, arrayList0Var);

		DefaultTestCase tc = builder.toTestCase();
		return tc;
	}

	@Test
	public void testStackOverflow() throws ClassNotFoundException, NoSuchMethodException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		Properties.TARGET_CLASS = ListItr.class.getName();

		DefaultTestCase tc = buildTestCase2();

		TestSuiteChromosome suite = new TestSuiteChromosome();
		suite.addTest(tc);

		EPATransitionCoverageSuiteFitness epaTransitionFitness = new EPATransitionCoverageSuiteFitness(xmlFilename);

		int expectedTotalTransitions = 69;
		int expectedCoveredTransitions = 0;

		List<ExecutionResult> results = new LinkedList<ExecutionResult>();
		for (ExecutableChromosome chromosome : suite.getTestChromosomes()) {
			ExecutionResult result = chromosome.executeForFitnessFunction(epaTransitionFitness);
			results.add(result);
		}
		assertEquals(1, results.size());
		ExecutionResult execResult = results.get(0);
		ExecutionTrace execTrace = execResult.getTrace();
		Collection<Throwable> thrownExceptions = execResult.getAllThrownExceptions();

		suite.addFitness(epaTransitionFitness);
		double fitnessValue = epaTransitionFitness.getFitness(suite);

		// fitness is the number of uncovered EPA transitions
		double expectedUncoveredTransitions = (double) expectedTotalTransitions - expectedCoveredTransitions;
		assertEquals(expectedUncoveredTransitions, fitnessValue, 0.00000001);

		double suiteFitnessValue = suite.getFitness();
		assertEquals(fitnessValue, suiteFitnessValue, 0.00000001);

	}

	@Before
	public void prepareTest() throws FileNotFoundException, ParserConfigurationException, SAXException, IOException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");
		final File epaXMLFile = new File(xmlFilename);
		Assume.assumeTrue(epaXMLFile.exists());

		Properties.EPA_XML_PATH = xmlFilename;


		DEFAULT_TIMEOUT = Properties.TIMEOUT;

		EPAMonitor.reset();
//		Properties.TIMEOUT = 15 * 1000;
	}

	@After
	public void tearDownTest() {
		Properties.EPA_XML_PATH = null;
		Properties.TIMEOUT = DEFAULT_TIMEOUT;
	}

	@Test
	public void testStatesAgainstEPA() throws ClassNotFoundException, NoSuchMethodException, FileNotFoundException,
			ParserConfigurationException, SAXException, IOException, MalformedEPATraceException {
		final String xmlFilename = String.join(File.separator, System.getProperty("user.dir"), "src", "test",
				"resources", "epas", "ListItr.xml");

		EPA epa = EPAFactory.buildEPA(xmlFilename);

		Properties.TARGET_CLASS = ListItr.class.getName();

		DefaultTestCase tc = buildTestCase0();

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
		List<EPATrace> traces = new LinkedList<EPATrace>(executionResult.getTrace().getEPATraces());

		assertEquals(1, traces.size());

		EPATrace trace = traces.get(0);
		assertEquals(2, trace.getEpaTransitions().size());

		EPATransition t1 = trace.getEpaTransitions().get(0);
		EPATransition t2 = trace.getEpaTransitions().get(1);

		EPAState sinit = new EPAState("Sinit");
		EPAState s87 = new EPAState("S87");
		EPAState s119 = new EPAState("S119");

		assertEquals(sinit, t1.getOriginState());
		assertEquals("ListItr()", t1.getActionName());
		assertEquals(s87, t1.getDestinationState());

		assertEquals(s87, t2.getOriginState());
		assertEquals("add()", t2.getActionName());
		assertEquals(s119, t2.getDestinationState());

		assertTrue(epa.containsAction("add()"));
		assertTrue(epa.containsAction("ListItr()"));

	}

}
