package org.evosuite.testcase;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestGenerationContext;
import org.evosuite.Properties.LocalSearchBudgetType;
import org.evosuite.coverage.branch.BranchCoverageSuiteFitness;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.localsearch.DefaultLocalSearchObjective;
import org.evosuite.ga.localsearch.LocalSearchObjective;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testcase.localsearch.BranchCoverageMap;
import org.evosuite.testcase.statements.FloatPrimitiveStatement;
import org.evosuite.testcase.statements.IntPrimitiveStatement;
import org.evosuite.testcase.statements.MethodStatement;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.evosuite.testsuite.localsearch.TestSuiteLocalSearch;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericConstructor;
import org.evosuite.utils.GenericMethod;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.localsearch.ArrayLocalSearchExample;
import com.examples.with.different.packagename.localsearch.DoubleLocalSearchExample;
import com.examples.with.different.packagename.localsearch.FloatLocalSearchExample;
import com.examples.with.different.packagename.localsearch.IntegerLocalSearchExample;
import com.examples.with.different.packagename.localsearch.StringLocalSearchExample;

public class TestLocalSearch extends SystemTest {

	private static final int oldSearchRate             = Properties.LOCAL_SEARCH_RATE;
	private static final double oldSearchProb          = Properties.LOCAL_SEARCH_PROBABILITY;
	private static final Properties.LocalSearchBudgetType localSearchBudgetType = Properties.LOCAL_SEARCH_BUDGET_TYPE;
	private static final long oldSearchBudget = Properties.LOCAL_SEARCH_BUDGET; 
	private static final boolean localSearchArrays     = Properties.LOCAL_SEARCH_ARRAYS;
	private static final boolean localSearchPrimitives = Properties.LOCAL_SEARCH_PRIMITIVES;
	private static final boolean localSearchReferences = Properties.LOCAL_SEARCH_REFERENCES;
	private static final int chromosomeLength          = Properties.CHROMOSOME_LENGTH;
	private static final int maxInitialTests           = Properties.MAX_INITIAL_TESTS;
	
	@After
	public void resetLocalSearchRate() {
		Properties.LOCAL_SEARCH_RATE = oldSearchRate;
		Properties.LOCAL_SEARCH_PROBABILITY = oldSearchProb;
		Properties.LOCAL_SEARCH_BUDGET_TYPE = localSearchBudgetType;
		Properties.LOCAL_SEARCH_BUDGET = oldSearchBudget;
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
		Properties.SEARCH_BUDGET = 20000;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
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
		Properties.SEARCH_BUDGET = 20000;
		
		// Make sure that local search will have effect
		Properties.CHROMOSOME_LENGTH = 5;
		Properties.MAX_INITIAL_TESTS = 2;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testIntLocalSearchOnTest() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = IntegerLocalSearchExample.class.getCanonicalName();
		Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		GenericClass clazz = new GenericClass(sut);
		Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_REFERENCES = false;
		Properties.LOCAL_SEARCH_ARRAYS = false;
		Properties.PRINT_TO_SYSTEM = true;

		DefaultTestCase test = new DefaultTestCase();
		GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

		TestFactory testFactory = TestFactory.getInstance();
		VariableReference callee = testFactory.addConstructor(test, gc, 0, 0);
		VariableReference intVar1 = test.addStatement(new IntPrimitiveStatement(test, 1));
		VariableReference intVar0 = test.addStatement(new IntPrimitiveStatement(test, 1));

		Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[] { int.class, int.class });
		GenericMethod method = new GenericMethod(m, sut);
		MethodStatement ms = new MethodStatement(test, method, callee, Arrays.asList(new VariableReference[] {intVar0, intVar1}));
		test.addStatement(ms);
		System.out.println(test);
		
		TestSuiteChromosome suite = new TestSuiteChromosome();
		BranchCoverageSuiteFitness fitness = new BranchCoverageSuiteFitness();

		BranchCoverageMap.getInstance().searchStarted(null);
		assertEquals(4.0, fitness.getFitness(suite), 0.1F);
		suite.addTest(test);
		assertEquals(1.0, fitness.getFitness(suite), 0.1F);
		
		TestSuiteLocalSearch localSearch = TestSuiteLocalSearch.getLocalSearch();
		LocalSearchObjective<TestSuiteChromosome> localObjective = new DefaultLocalSearchObjective<TestSuiteChromosome>(fitness);
		localSearch.doSearch(suite, localObjective);
		System.out.println("Fitness: "+fitness.getFitness(suite));
		System.out.println("Test suite: "+suite);
		assertEquals(0.0, fitness.getFitness(suite), 0.1F);
		BranchCoverageMap.getInstance().searchFinished(null);
	}
	
	@Test
	public void testFloatGlobalSearch() {
		EvoSuite evosuite = new EvoSuite();

		String targetClass = FloatLocalSearchExample.class.getCanonicalName();

		Properties.TARGET_CLASS = targetClass;
		// Properties.SEARCH_BUDGET = 20000;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertTrue("Did not expect optimal coverage", best.getCoverage() < 1.0);
	}
	
	@Test
	public void testFloatLocalSearchOnTest() throws ClassNotFoundException, ConstructionFailedException, NoSuchMethodException, SecurityException {
		Properties.TARGET_CLASS = FloatLocalSearchExample.class.getCanonicalName();
		Class<?> sut = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(Properties.TARGET_CLASS);
		GenericClass clazz = new GenericClass(sut);
		Properties.LOCAL_SEARCH_BUDGET_TYPE = LocalSearchBudgetType.TESTS;
		Properties.LOCAL_SEARCH_REFERENCES = false;
		Properties.LOCAL_SEARCH_ARRAYS = false;

		DefaultTestCase test = new DefaultTestCase();
		GenericConstructor gc = new GenericConstructor(clazz.getRawClass().getConstructors()[0], clazz);

		TestFactory testFactory = TestFactory.getInstance();
		VariableReference callee = testFactory.addConstructor(test, gc, 0, 0);
		VariableReference floatVar0 = test.addStatement(new FloatPrimitiveStatement(test, 1F));
		VariableReference floatVar1 = test.addStatement(new FloatPrimitiveStatement(test, 1F));

		Method m = clazz.getRawClass().getMethod("testMe", new Class<?>[] { float.class, float.class });
		GenericMethod method = new GenericMethod(m, sut);
		MethodStatement ms = new MethodStatement(test, method, callee, Arrays.asList(new VariableReference[] {floatVar0, floatVar1}));
		test.addStatement(ms);
		System.out.println(test);
		
		TestSuiteChromosome suite = new TestSuiteChromosome();
		BranchCoverageSuiteFitness fitness = new BranchCoverageSuiteFitness();

		BranchCoverageMap.getInstance().searchStarted(null);
		assertEquals(4.0, fitness.getFitness(suite), 0.1F);
		suite.addTest(test);
		assertEquals(1.0, fitness.getFitness(suite), 0.1F);

		System.out.println("Test suite: "+suite);
		
		TestSuiteLocalSearch localSearch = TestSuiteLocalSearch.getLocalSearch();
		LocalSearchObjective<TestSuiteChromosome> localObjective = new DefaultLocalSearchObjective<TestSuiteChromosome>(fitness);
		localSearch.doSearch(suite, localObjective);
		System.out.println("Fitness: "+fitness.getFitness(suite));
		System.out.println("Test suite: "+suite);
		assertEquals(0.0, fitness.getFitness(suite), 0.1F);
		BranchCoverageMap.getInstance().searchFinished(null);
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
		// Properties.SEARCH_BUDGET = 20000;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
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
		//Properties.SEARCH_BUDGET = 30000;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
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
		//Properties.SEARCH_BUDGET = 30000;

		// Make sure that local search will have effect
		Properties.CHROMOSOME_LENGTH = 5;
		Properties.MAX_INITIAL_TESTS = 2;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
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
		//Properties.SEARCH_BUDGET = 20000;
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
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
		//Properties.SEARCH_BUDGET = 30000;
		
		// Make sure that local search will have effect
		Properties.CHROMOSOME_LENGTH = 5;
		Properties.MAX_INITIAL_TESTS = 2;

		String[] command = new String[] { "-generateSuite", "-class", targetClass };

		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
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
		GeneticAlgorithm<?> ga = getGAFromResult(result);
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
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n" + best);

		// int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		// Assert.assertEquals("Wrong number of goals: ", 3, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
