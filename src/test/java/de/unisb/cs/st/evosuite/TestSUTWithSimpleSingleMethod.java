package de.unisb.cs.st.evosuite;

import org.junit.*;

import com.examples.with.different.packagename.SingleMethod;

/**
 * @author Andrea Arcuri
 * 
 */
public class TestSUTWithSimpleSingleMethod {

	@Test
	public void testSingleMethod(){
		EvoSuite evosuite = new EvoSuite();
		
		String[] command = new String[]{				
				EvoSuite.JAVA_CMD,
				"-generateSuite",
				"-class",
				SingleMethod.class.getCanonicalName(),
				"-Dgenerations=1"
		};
		
		Object result = evosuite.parseCommandLine(command);
		
		Assert.assertTrue(result != null);
		
		//TODO, check best individual has only one method, and calling it should return "foo"
	}
}
