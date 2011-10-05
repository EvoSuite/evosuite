package de.unisb.cs.st.evosuite;

import org.junit.*;

import com.examples.with.different.packagename.SingleMethod;

import de.unisb.cs.st.evosuite.ga.GeneticAlgorithm;

/**
 * @author Andrea Arcuri
 * 
 */
public class TestSUTWithSimpleSingleMethod extends SystemTest{

	@Test
	public void testSingleMethod(){
		EvoSuite evosuite = new EvoSuite();
		
		String[] command = new String[]{				
				//EvoSuite.JAVA_CMD,
				"-generateSuite",
				"-class",
				SingleMethod.class.getCanonicalName(),
				"-Dhtml=false",
				"-Dplot=false",
				"-Djunit_tests=false",
				"-Dshow_progress=false",
				"-Dgenerations=1"
		};
		
		Object result = evosuite.parseCommandLine(command);
		
		Assert.assertTrue(result != null);
		Assert.assertTrue("Invalid result type :"+result.getClass(), result instanceof GeneticAlgorithm);
		
		//TODO, check best individual has only one method, and calling it should return "foo"
	}
}
