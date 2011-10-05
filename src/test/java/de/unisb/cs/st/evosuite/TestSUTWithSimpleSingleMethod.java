package de.unisb.cs.st.evosuite;

import org.junit.*;

import com.examples.with.different.packagename.SingleMethod;

import de.unisb.cs.st.evosuite.ga.Chromosome;
import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;

/**
 * @author Andrea Arcuri
 * 
 */
public class TestSUTWithSimpleSingleMethod extends SystemTest{

	@Test
	public void testSingleMethod(){
		EvoSuite evosuite = new EvoSuite();
		int generations = 1;
		
		String targetClass = SingleMethod.class.getCanonicalName();
		Properties.TARGET_CLASS = targetClass; //needed, otherwise exception in TestCluster when reading results
		
		String[] command = new String[]{				
				//EvoSuite.JAVA_CMD,
				"-generateTests",
				"-class",
				targetClass,
				"-Dhtml=false",
				"-Dplot=false",
				"-Djunit_tests=false",
				"-Dshow_progress=false",
				"-Dgenerations="+generations
		};
		
		Object result = evosuite.parseCommandLine(command);
		
		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :"+result.getClass(), result instanceof GeneticAlgorithm);
		
		GeneticAlgorithm ga = (GeneticAlgorithm) result;
		Assert.assertEquals("Wrong number of generations: ", generations, ga.getAge());
		Chromosome best = ga.getBestIndividual();
		Assert.assertEquals("Wrong number of statements: ",2 , best.size());
		
		//TODO, check best individual has only one method, and calling it should return "foo"
	}
}
