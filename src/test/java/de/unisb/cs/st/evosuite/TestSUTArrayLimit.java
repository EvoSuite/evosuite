package de.unisb.cs.st.evosuite;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.examples.with.different.packagename.ArrayLimit;
import com.examples.with.different.packagename.DivisionByZero;

import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;




public class TestSUTArrayLimit extends SystemTest{

	public static final int defaultArrayLimit = Properties.ARRAY_LIMIT;

	@After
	public void resetProperties(){
		Properties.ARRAY_LIMIT = defaultArrayLimit;
	}


	@Test
	public void testWithinLimits() {
		EvoSuite evosuite = new EvoSuite();
		String targetClass = ArrayLimit.class.getCanonicalName();
		
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

		Assert.assertEquals("Non-optimal coverage: ",1d, best.getCoverage(), 0.001);
	}

	@Test
	public void testAboveLimits() {
		EvoSuite evosuite = new EvoSuite();
		String targetClass = ArrayLimit.class.getCanonicalName();
		
		Properties.ARRAY_LIMIT = 10;
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

		Assert.assertTrue("Optimal coverage: ", best.getCoverage() < 0.99);
	}

}
