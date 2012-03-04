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
				
		String targetClass = HighConstant.class.getCanonicalName();
		
		Properties.GENERATIONS = 1;
		Properties.TARGET_CLASS = targetClass;
		Properties.HTML = false;
		Properties.SHOW_PROGRESS = false;
		Properties.SERIALIZE_RESULT = false;
		Properties.JUNIT_TESTS = false;
		Properties.PLOT = false;
		
		Properties.CLIENT_ON_THREAD = true;
		
		Properties.PRIMITIVE_POOL = 0;
		
		String[] command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass
		};
		
		Object result = evosuite.parseCommandLine(command);
		
		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :"+result.getClass(), result instanceof GeneticAlgorithm);
		
		GeneticAlgorithm ga = (GeneticAlgorithm) result;
		Assert.assertEquals("Wrong number of generations: ", 0, ga.getAge());
		TestSuiteChromosome best = (TestSuiteChromosome)ga.getBestIndividual();
		Assert.assertEquals("Wrong number of test cases: ",1 , best.size());
		/*
		 * there are 2 branches and one method, so 3 targets, of which we cover only 2
		 */
		Assert.assertEquals("Non-optimal coverage: ",2d/3d, best.getCoverage(), 0.001);
		/*
		 * - Constructor
		 * - variable init
		 * - method call
		 */
		Assert.assertEquals("Wrong number of statements: ",3,best.getTestChromosome(0).getTestCase().size());
	}
	
	@Test
	public void testUsingPrimitivePool(){
		EvoSuite evosuite = new EvoSuite();
		
		String targetClass = HighConstant.class.getCanonicalName();
		
		Properties.GENERATIONS = 1;
		Properties.TARGET_CLASS = targetClass;
		Properties.HTML = false;
		Properties.SHOW_PROGRESS = false;
		Properties.SERIALIZE_RESULT = false;
		Properties.JUNIT_TESTS = false;
		Properties.PLOT = false;
		
		Properties.CLIENT_ON_THREAD = true;
		
		Properties.PRIMITIVE_POOL = 0.8;
		
		String[] command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass
		};
		
		Object result = evosuite.parseCommandLine(command);
		
		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :"+result.getClass(), result instanceof GeneticAlgorithm);
		
		GeneticAlgorithm ga = (GeneticAlgorithm) result;
		Assert.assertEquals("Wrong number of generations: ", 0, ga.getAge());
		TestSuiteChromosome best = (TestSuiteChromosome)ga.getBestIndividual();
		Assert.assertEquals("Wrong number of test cases: ",2 , best.size());
		Assert.assertEquals("Non-optimal coverage: ",1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Wrong number of statements: ",3,best.getTestChromosome(0).getTestCase().size());
		Assert.assertEquals("Wrong number of statements: ",3,best.getTestChromosome(1).getTestCase().size());
	}
}
