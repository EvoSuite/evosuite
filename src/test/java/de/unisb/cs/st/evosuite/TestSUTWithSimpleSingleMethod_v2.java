package de.unisb.cs.st.evosuite;

import org.junit.*;

import com.examples.with.different.packagename.SingleMethod;

import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;
import de.unisb.cs.st.evosuite.testsuite.TestSuiteChromosome;

/**
 * @author Andrea Arcuri
 * 
 */
public class TestSUTWithSimpleSingleMethod_v2 extends SystemTest{

	@Test
	public void testSingleMethod(){
		EvoSuite evosuite = new EvoSuite();
		
		String targetClass = SingleMethod.class.getCanonicalName();
		
		Properties.GENERATIONS = 1;
		Properties.TARGET_CLASS = targetClass;
		Properties.HTML = false;
		Properties.SHOW_PROGRESS = false;
		Properties.SERIALIZE_RESULT = false;
		Properties.JUNIT_TESTS = false;
		Properties.PLOT = false;
		
		Properties.CLIENT_ON_THREAD = true;
		
		String[] command = new String[]{				
				"-generateTests",
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
		Assert.assertEquals("Non-optimal coverage: ",1d, best.getCoverage(), 0.001);
		Assert.assertEquals("Wrong number of statements: ",2,best.getTestChromosome(0).getTestCase().size());
	}
}
