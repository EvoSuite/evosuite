package org.evosuite.coverage.epa;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTestBase;
import org.evosuite.Properties.StoppingCondition;
import org.evosuite.ga.FitnessFunction;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testcase.execution.TestCaseExecutor;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.examples.with.different.packagename.epa.BoundedStack;
import com.examples.with.different.packagename.epa.MiniBoundedStack;

public class EPAMiningSystemTest extends SystemTestBase {

	@Before
	public void prepare() {
		Properties.ASSERTIONS = true;
		Properties.P_FUNCTIONAL_MOCKING = 0.0;
		Properties.P_REFLECTION_ON_PRIVATE = 0.0;
	}

	@Test
	public void testMiningEPAWithNoMinimizationNoArchive() {
		mineEPAOfBoundedStack(false, false);
	}

	@Test
	public void testMiningEPAWithMinimizationNoArchive() {
		mineEPAOfBoundedStack(true, false);
	}

	private void mineEPAOfBoundedStack(boolean minimize, boolean archive) {
		Properties.TEST_ARCHIVE = archive;
		Properties.MINIMIZE = minimize;
		Properties.STOPPING_CONDITION = StoppingCondition.MAXGENERATIONS;
		Properties.SEARCH_BUDGET = 10;
		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.EPAMINING };
		Properties.CLIENT_ON_THREAD = true;

		// check test case
		final String targetClass = MiniBoundedStack.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		final EvoSuite evoSuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		final Object results = evoSuite.parseCommandLine(command);
		Assert.assertNotNull(results);
		GeneticAlgorithm<?> ga = getGAFromResult(results);

		assertEquals(3, EPAMiningCoverageFactory.getGoals().size());

		TestSuiteChromosome bestIndividual = (TestSuiteChromosome) ga.getBestIndividual();
		assertTrue(!bestIndividual.getTests().isEmpty());

		TestCase test = bestIndividual.getTests().get(0);
		assertTrue(!test.isEmpty());

		String individual = bestIndividual.toString();
		System.out.println("===========================");
		System.out.println("Best Individual:");
		System.out.println(individual);
		System.out.println("===========================");

		// Since there is an unknown number of goals to begin with, the number of goals
		// is always 0
		int numOfCoveredGoals = bestIndividual.getNumOfCoveredGoals();
		int expectedNumberOfTransitions = 3;
		assertEquals(expectedNumberOfTransitions, numOfCoveredGoals);

		double coverage = bestIndividual.getCoverage();
		double expectedCoverage = 1.0;
		assertEquals(expectedCoverage, coverage, 0.000001);

		// there is a single fitness function
		assertEquals(1, ga.getFitnessFunctions().size());
		FitnessFunction<TestSuiteChromosome> ff = (FitnessFunction<TestSuiteChromosome>) ga.getFitnessFunctions()
				.get(0);

		int numberOfActions = 3;
		int maxNumberOfStates = (int) Math.pow(2, numberOfActions);
		int maxNumberOfTransitions = maxNumberOfStates * numberOfActions * maxNumberOfStates;
		int expectedFitness = maxNumberOfTransitions - expectedNumberOfTransitions;

		double fitness = bestIndividual.getFitness(ff);
		assertEquals(expectedFitness, (int) fitness);

	}

	@Test
	public void testMiningEPAWithArchive() {
		mineEPAOfBoundedStack(true, true);
	}

	@Test
	public void testMiningEPAOfBoundedStack() {
		Properties.TEST_ARCHIVE = true;
		Properties.MINIMIZE = true;
		Properties.STOPPING_CONDITION = StoppingCondition.MAXGENERATIONS;
		Properties.SEARCH_BUDGET = 10;
		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.EPAMINING };
		Properties.CLIENT_ON_THREAD = true;

		// check test case
		final String targetClass = BoundedStack.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass;

		final EvoSuite evoSuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		final Object results = evoSuite.parseCommandLine(command);
		Assert.assertNotNull(results);
		GeneticAlgorithm<?> ga = getGAFromResult(results);

		TestSuiteChromosome bestIndividual = (TestSuiteChromosome) ga.getBestIndividual();
		assertTrue(!bestIndividual.getTests().isEmpty());

		TestCase test = bestIndividual.getTests().get(0);
		assertTrue(!test.isEmpty());

		String individual = bestIndividual.toString();
		System.out.println("===========================");
		System.out.println("Best Individual:");
		System.out.println(individual);
		System.out.println("===========================");

		assertEquals(8, EPAMiningCoverageFactory.getGoals().size());

		// Since there is an unknown number of goals to begin with, the number of goals
		// is always 0
		int numOfCoveredGoals = bestIndividual.getNumOfCoveredGoals();
		int expectedNumberOfTransitions = 8;
		assertEquals(expectedNumberOfTransitions, numOfCoveredGoals);

		double coverage = bestIndividual.getCoverage();
		double expectedCoverage = 1.0;
		assertEquals(expectedCoverage, coverage, 0.000001);

		// there is a single fitness function
		assertEquals(1, ga.getFitnessFunctions().size());
		FitnessFunction<TestSuiteChromosome> ff = (FitnessFunction<TestSuiteChromosome>) ga.getFitnessFunctions()
				.get(0);

		int numberOfActions = 3;
		int maxNumberOfStates = (int) Math.pow(2, numberOfActions);
		int maxNumberOfTransitions = maxNumberOfStates * numberOfActions * maxNumberOfStates;
		int expectedFitness = maxNumberOfTransitions - expectedNumberOfTransitions;

		double fitness = bestIndividual.getFitness(ff);
		assertEquals(expectedFitness, (int) fitness);

	}

	@Test
	public void testSimpleEPAFromTraces() throws MalformedEPATraceException {
		Properties.TEST_ARCHIVE = false;
		Properties.MINIMIZE = true;
		Properties.STOPPING_CONDITION = StoppingCondition.MAXGENERATIONS;
		Properties.SEARCH_BUDGET = 60;
		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.EPAMINING };
		Properties.CLIENT_ON_THREAD = true;
		Properties.ASSERTIONS = false;

		final String targetClass = MiniBoundedStack.class.getCanonicalName();

		EPA inferredAutomata = inferAutomata(targetClass);

		int expectedNumberOfStates = 3;
		assertEquals(expectedNumberOfStates, inferredAutomata.getStates().size());

		int expectedNumberOfActions = 3;
		assertEquals(expectedNumberOfActions, inferredAutomata.getActions().size());

		int expectedNumberOfTransitions = 3;
		assertEquals(expectedNumberOfTransitions, inferredAutomata.getTransitions().size());

		EPADotPrinter printer = new EPADotPrinter();
		String dot_str = printer.toDot(inferredAutomata);
		assertNotNull(dot_str);
	}

	@Test
	public void testWriteToXMLFromTraces()
			throws MalformedEPATraceException, ParserConfigurationException, SAXException, IOException {
		Properties.TEST_ARCHIVE = false;
		Properties.MINIMIZE = true;
		Properties.STOPPING_CONDITION = StoppingCondition.MAXGENERATIONS;
		Properties.SEARCH_BUDGET = 60;
		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.EPAMINING };
		Properties.CLIENT_ON_THREAD = true;
		Properties.ASSERTIONS = false;
		Properties.INFERRED_EPA_XML_PATH = "inferred_bounded_stack.xml";

		final String targetClass = MiniBoundedStack.class.getCanonicalName();
		EPA inferredAutomata = inferAutomata(targetClass);
		assertNotNull(inferredAutomata);

		File inferredEPAFile = new File(Properties.INFERRED_EPA_XML_PATH);
		assertTrue(inferredEPAFile.exists());

		EPA epa = EPAFactory.buildEPA(Properties.INFERRED_EPA_XML_PATH);

		int expectedNumberOfStates = 3;
		assertEquals(expectedNumberOfStates, epa.getStates().size());

		int expectedNumberOfActions = 3;
		assertEquals(expectedNumberOfActions, epa.getActions().size());

		int expectedNumberOfTransitions = 3;
		assertEquals(expectedNumberOfTransitions, epa.getTransitions().size());

	}

	@Test
	public void testEPAFromTraces() throws MalformedEPATraceException {
		Properties.TEST_ARCHIVE = false;
		Properties.MINIMIZE = true;
		Properties.STOPPING_CONDITION = StoppingCondition.MAXGENERATIONS;
		Properties.SEARCH_BUDGET = 60;
		Properties.CRITERION = new Properties.Criterion[] { Properties.Criterion.EPAMINING };
		Properties.CLIENT_ON_THREAD = true;
		Properties.ASSERTIONS = false;

		final String targetClass = BoundedStack.class.getCanonicalName();

		EPA inferredAutomata = inferAutomata(targetClass);

		int expectedNumberOfStates = 5;
		assertEquals(expectedNumberOfStates, inferredAutomata.getStates().size());

		int expectedNumberOfActions = 3;
		assertEquals(expectedNumberOfActions, inferredAutomata.getActions().size());

		int expectedNumberOfTransitions = 10;
		assertEquals(expectedNumberOfTransitions, inferredAutomata.getTransitions().size());

		EPADotPrinter printer = new EPADotPrinter();
		String dot_str = printer.toDot(inferredAutomata);
		assertNotNull(dot_str);
	}

	private EPA inferAutomata(final String targetClass) throws MalformedEPATraceException {
		Properties.TARGET_CLASS = targetClass;

		final EvoSuite evoSuite = new EvoSuite();
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		final Object results = evoSuite.parseCommandLine(command);
		Assert.assertNotNull(results);
		GeneticAlgorithm<?> ga = getGAFromResult(results);
		TestSuiteChromosome bestIndividual = (TestSuiteChromosome) ga.getBestIndividual();

		TestCaseExecutor.initExecutor();
		EPAMonitor.reset();
		EPAMonitor.getInstance().setEnabled(true);
		for (TestChromosome test : bestIndividual.getTestChromosomes()) {
			TestCase testCase = test.getTestCase();
			ExecutionResult executionResult = TestCaseExecutor.runTest(testCase);
			test.setLastExecutionResult(executionResult);
		}

		Set<EPATrace> traces = new HashSet<EPATrace>();
		for (ExecutionResult result : bestIndividual.getLastExecutionResults()) {
			Set<EPATrace> resultTraces = result.getTrace().getEPATraces();
			traces.addAll(resultTraces);
		}
		EPA inferredAutomata = EPAFactory.buildEPA(traces);
		return inferredAutomata;
	}

}
