package de.unisb.cs.st.evosuite;

import org.junit.*;

import com.examples.with.different.packagename.HighConstant;
import com.examples.with.different.packagename.SingleMethod;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author Andrea Arcuri
 * 
 */
public class TestSUTHighConstant extends SystemTest{

	@Test
	public void testNoPrimitivePool(){
		EvoSuite evosuite = new EvoSuite();
		int generations = 1;
		
		String targetClass = HighConstant.class.getCanonicalName();
		
		String[] command = new String[]{				
				"-generateTests",
				"-class",
				targetClass,
				"-Dhtml=false",
				"-Dplot=false",
				"-Djunit_tests=false",
				"-Dshow_progress=false",
				"-Dgenerations="+generations,
				"-Dprimitive_pool=0"
		};
		
		Object result = evosuite.parseCommandLine(command);
		
		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :"+result.getClass(), result instanceof GeneticAlgorithm);
		
		GeneticAlgorithm ga = (GeneticAlgorithm) result;
		Assert.assertEquals("Wrong number of generations: ", 0, ga.getAge());
		TestSuiteChromosome best = (TestSuiteChromosome)ga.getBestIndividual();
		Assert.assertEquals("Wrong number of test cases: ",1 , best.size());
		Assert.assertEquals("Non-optimal coverage: ",0.5, best.getCoverage(), 0.001);
		Assert.assertEquals("Wrong number of statements: ",2,best.getTestChromosome(0).getTestCase().size());
	}
	
	@Test
	public void testUsingPrimitivePool(){
		EvoSuite evosuite = new EvoSuite();
		int generations = 1;
		
		String targetClass = HighConstant.class.getCanonicalName();
		
		String[] command = new String[]{				
				"-generateTests",
				"-class",
				targetClass,
				"-Dhtml=false",
				"-Dplot=false",
				"-Djunit_tests=false",
				"-Dshow_progress=false",
				"-Dgenerations="+generations,
				"-Dprimitive_pool=0.2"
		};
		
		Object result = evosuite.parseCommandLine(command);
		
		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :"+result.getClass(), result instanceof GeneticAlgorithm);
		
		GeneticAlgorithm ga = (GeneticAlgorithm) result;
		Assert.assertEquals("Wrong number of generations: ", 0, ga.getAge());
		TestSuiteChromosome best = (TestSuiteChromosome)ga.getBestIndividual();
		Assert.assertEquals("Wrong number of test cases: ",1 , best.size());
		Assert.assertEquals("Non-optimal coverage: ",1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Wrong number of statements: ",3,best.getTestChromosome(0).getTestCase().size());
	}
}
