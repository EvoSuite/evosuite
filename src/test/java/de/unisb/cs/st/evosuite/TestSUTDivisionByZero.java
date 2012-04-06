package de.unisb.cs.st.evosuite;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.examples.with.different.packagename.DivisionByZero;

import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

public class TestSUTDivisionByZero extends SystemTest{

	/*
	 * To avoid side effects on test cases that we will run afterwards,
	 * if we modify some values in Properties, then we need to re-int them after
	 * each test case execution
	 */
	public static final double defaultPrimitivePool = Properties.PRIMITIVE_POOL;
	public static final boolean defaultErrorBranches = Properties.ERROR_BRANCHES;
	
	@After
	public void resetProperties(){
		Properties.PRIMITIVE_POOL = defaultPrimitivePool;
		Properties.ERROR_BRANCHES = defaultErrorBranches;
	}
	
	@Ignore
	@Test
	public void testDivisonByZero(){
		EvoSuite evosuite = new EvoSuite();
				
		String targetClass = DivisionByZero.class.getCanonicalName();
		
		Properties.TARGET_CLASS = targetClass;
		Properties.PRIMITIVE_POOL = 0.99;
		Properties.ERROR_BRANCHES = true;
		
		String[] command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass
		};
		
		Object result = evosuite.parseCommandLine(command);
		
		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :"+result.getClass(), result instanceof GeneticAlgorithm);
		
		GeneticAlgorithm ga = (GeneticAlgorithm) result;
		TestSuiteChromosome best = (TestSuiteChromosome)ga.getBestIndividual();
		System.out.println("EvolvedTestSuite:\n"+best);

		int goals = TestSuiteGenerator.getFitnessFactory().getCoverageGoals().size();
		Assert.assertEquals("Wrong number of goals: ",3 , goals);
		Assert.assertEquals("Non-optimal coverage: ",1d, best.getCoverage(), 0.001);
	}
}
