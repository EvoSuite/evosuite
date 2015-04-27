package org.evosuite.coverage.mutation;

import java.util.Arrays;

import org.evosuite.EvoSuite;
import org.evosuite.Properties;
import org.evosuite.SystemTest;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.Properties.Criterion;
import org.evosuite.ga.metaheuristics.GeneticAlgorithm;
import org.evosuite.testsuite.TestSuiteChromosome;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.mutation.MutationPropagation;
import com.examples.with.different.packagename.mutation.SimpleMutationExample1;
import com.examples.with.different.packagename.mutation.SimpleMutationExample2;

public class TestMutation extends SystemTest {

	private Properties.Criterion[] oldCriteria = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length); 
	private Properties.StoppingCondition oldStoppingCondition = Properties.STOPPING_CONDITION; 
	private double oldPrimitivePool = Properties.PRIMITIVE_POOL;
	
	@Before
	public void beforeTest() {
		oldCriteria = Arrays.copyOf(Properties.CRITERION, Properties.CRITERION.length);
		oldStoppingCondition = Properties.STOPPING_CONDITION;
		oldPrimitivePool = Properties.PRIMITIVE_POOL;
		//Properties.MINIMIZE = false;
	}
	
	@After
	public void restoreProperties() {
		Properties.CRITERION = oldCriteria;
		Properties.STOPPING_CONDITION = oldStoppingCondition;
		Properties.PRIMITIVE_POOL = oldPrimitivePool;
	}

	@Test
	public void testWeakMutationSimpleExampleWithArchive() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.WEAKMUTATION };

		String targetClass = SimpleMutationExample1.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(12, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testWeakMutationSimpleExampleWithoutArchive() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.WEAKMUTATION };

		String targetClass = SimpleMutationExample1.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(12, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testStrongMutationSimpleExampleWithArchive() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.STRONGMUTATION };

		String targetClass = SimpleMutationExample1.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(12, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testStrongMutationSimpleExampleWithoutArchive() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.STRONGMUTATION };

		String targetClass = SimpleMutationExample1.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(12, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testWeakMutationSimpleExampleWithArchive2() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.WEAKMUTATION };

		String targetClass = SimpleMutationExample2.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(22, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testWeakMutationSimpleExampleWithoutArchive2() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.WEAKMUTATION };

		String targetClass = SimpleMutationExample2.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(22, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testStrongMutationSimpleExampleWithArchive2() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.STRONGMUTATION };

		String targetClass = SimpleMutationExample2.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(22, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testStrongMutationSimpleExampleWithoutArchive2() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.STRONGMUTATION };

		String targetClass = SimpleMutationExample2.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(22, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testWeakMutationPropagationExampleWithArchive() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.WEAKMUTATION };

		String targetClass = MutationPropagation.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(24, goals);
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testWeakMutationPropagationExampleWithoutArchive() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.WEAKMUTATION };

		String targetClass = MutationPropagation.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(24, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testStrongMutationPropagationExampleWithArchive() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = true;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.STRONGMUTATION };

		String targetClass = MutationPropagation.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(24, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
	
	@Test
	public void testStrongMutationPropagationExampleWithoutArchive() {
		EvoSuite evosuite = new EvoSuite();
		boolean archive = Properties.TEST_ARCHIVE;
		Properties.TEST_ARCHIVE = false;
        Properties.CRITERION = new Properties.Criterion[] { Criterion.STRONGMUTATION };

		String targetClass = MutationPropagation.class.getCanonicalName();
		
		String[] command = new String[] { "-generateSuite", "-class", targetClass };
		Object result = evosuite.parseCommandLine(command);
		GeneticAlgorithm<?> ga = getGAFromResult(result);
		TestSuiteChromosome best = (TestSuiteChromosome) ga.getBestIndividual();
		Properties.TEST_ARCHIVE = archive;
		
		System.out.println("CoveredGoals:\n" + best.getCoveredGoals());
		System.out.println("EvolvedTestSuite:\n" + best);
		int goals = TestSuiteGenerator.getFitnessFactory().get(0).getCoverageGoals().size(); // assuming single fitness function
		Assert.assertEquals(24, goals );
		Assert.assertEquals("Non-optimal coverage: ", 1d, best.getCoverage(), 0.001);
	}
}
