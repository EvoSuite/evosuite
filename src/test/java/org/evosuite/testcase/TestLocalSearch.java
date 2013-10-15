package org.evosuite.testcase;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.Properties.LocalSearchBudgetType;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.localsearch.ArrayLocalSearchExample;
import com.examples.with.different.packagename.localsearch.DoubleLocalSearchExample;
import com.examples.with.different.packagename.localsearch.FloatLocalSearchExample;
import com.examples.with.different.packagename.localsearch.IntegerLocalSearchExample;
import com.examples.with.different.packagename.localsearch.StringLocalSearchExample;

public class TestLocalSearch extends SystemTest {

	private int oldSearchRate             = Properties.LOCAL_SEARCH_RATE;
	private double oldSearchProb          = Properties.LOCAL_SEARCH_PROBABILITY;
	private Properties.LocalSearchBudgetType localSearchBudgetType = Properties.LOCAL_SEARCH_BUDGET_TYPE;
	private boolean localSearchArrays     = Properties.LOCAL_SEARCH_ARRAYS;
	private boolean localSearchPrimitives = Properties.LOCAL_SEARCH_PRIMITIVES;
	private boolean localSearchReferences = Properties.LOCAL_SEARCH_REFERENCES;
	private int chromosomeLength          = Properties.CHROMOSOME_LENGTH;
	private int maxInitialTests           = Properties.MAX_INITIAL_TESTS;
	
	@After
	public void resetLocalSearchRate() {
		Properties.LOCAL_SEARCH_RATE = oldSearchRate;
		Properties.LOCAL_SEARCH_PROBABILITY = oldSearchProb;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = localSearchBudgetType;
		Properties.LOCAL_SEARCH_PRIMITIVES = localSearchPrimitives;
		Properties.LOCAL_SEARCH_REFERENCES = localSearchReferences;
		Properties.LOCAL_SEARCH_ARRAYS     = localSearchArrays;
		Properties.CHROMOSOME_LENGTH = chromosomeLength;
		Properties.MAX_INITIAL_TESTS = maxInitialTests;
	}
	
	
	
	@Test
	public void testIntegerGlobalSearch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = IntegerLocalSearchExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertTrue("Did not expect optimal coverage", best.getCoverage() < 1.0);
	}
	
	@Test
	public void testIntegerLocalSearch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = IntegerLocalSearchExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.LOCAL_SEARCH_RATE = 1;
		Properties.LOCAL_SEARCH_PROBABILITY = 1.0;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_REFERENCES = false;
		Properties.LOCAL_SEARCH_ARRAYS = false;
		
		// Make sure that local search will have effect
		Properties.CHROMOSOME_LENGTH = 5;
		Properties.MAX_INITIAL_TESTS = 2;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testFloatGlobalSearch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = FloatLocalSearchExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertTrue("Did not expect optimal coverage", best.getCoverage() < 1.0);
	}
	
	@Test
	public void testFloatLocalSearch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = FloatLocalSearchExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.LOCAL_SEARCH_RATE = 2;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_REFERENCES = false;
		Properties.LOCAL_SEARCH_ARRAYS = false;
		
		// Make sure that local search will have effect
		Properties.CHROMOSOME_LENGTH = 5;
		Properties.MAX_INITIAL_TESTS = 2;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testDoubleGlobalSearch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DoubleLocalSearchExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 30000;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertTrue("Did not expect optimal coverage", best.getCoverage() < 1.0);
	}
	
	@Test
	public void testDoubleLocalSearch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = DoubleLocalSearchExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.LOCAL_SEARCH_RATE = 2;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_REFERENCES = false;
		Properties.LOCAL_SEARCH_ARRAYS = false;
		Properties.SEARCH_BUDGET = 30000;

		// Make sure that local search will have effect
		Properties.CHROMOSOME_LENGTH = 5;
		Properties.MAX_INITIAL_TESTS = 2;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testStringGlobalSearch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = StringLocalSearchExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 20000;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertTrue("Did not expect optimal coverage", best.getCoverage() < 1.0);
	}
	
	@Test
	public void testStringLocalSearch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = StringLocalSearchExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.LOCAL_SEARCH_RATE = 2;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_REFERENCES = false;
		Properties.LOCAL_SEARCH_ARRAYS = false;
		Properties.SEARCH_BUDGET = 20000;
		
		// Make sure that local search will have effect
		Properties.CHROMOSOME_LENGTH = 5;
		Properties.MAX_INITIAL_TESTS = 2;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testArrayGlobalSearch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ArrayLocalSearchExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.SEARCH_BUDGET = 50000;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertTrue("Did not expect optimal coverage", best.getCoverage() < 1.0);
	}
	
	@Test
	public void testArrayLocalSearch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = ArrayLocalSearchExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		Properties.LOCAL_SEARCH_RATE = 2;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.SUITES;
		Properties.LOCAL_SEARCH_BUDGET = 1;
		Properties.LOCAL_SEARCH_REFERENCES = false;
		Properties.LOCAL_SEARCH_ARRAYS = true;
		Properties.SEARCH_BUDGET = 50000;
		
		// Make sure that local search will have effect
		Properties.CHROMOSOME_LENGTH = 5;
		Properties.MAX_INITIAL_TESTS = 2;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);

		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :" + result.getClass(),
		                  result instanceof GeneticAlgorithm);

		GeneticAlgorithm<?> ga = (GeneticAlgorithm<?>) result;
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
