package de.unisb.cs.st.evosuite;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.examples.with.different.packagename.NullString;

import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

public class TestNullString extends SystemTest{

	@Ignore
	@Test
	public void testNullString(){
		EvoSuite evosuite = new EvoSuite();
				
		String targetClass = NullString.class.getCanonicalName();
		
		Properties.TARGET_CLASS = targetClass;
		
		
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
