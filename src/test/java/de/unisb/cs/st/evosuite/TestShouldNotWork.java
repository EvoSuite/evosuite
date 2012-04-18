package de.unisb.cs.st.evosuite;

import static org.junit.Assert.fail;

import org.junit.Test;



public class TestShouldNotWork {

	@Test
	public void testShouldNotWorkOnJavaPackage(){
		EvoSuite evosuite = new EvoSuite();
		
		String targetClass = java.util.TreeMap.class.getCanonicalName();
		
		Properties.TARGET_CLASS = targetClass;
		
		String[] command = new String[]{				
				"-generateSuite",
				"-class",
				targetClass
		};

		try{
			Object result = evosuite.parseCommandLine(command);
		} catch(IllegalArgumentException e){
			//as expected
			System.out.println(e.toString());
			return;
		}
		
		fail("An exception should have been thrown");
	}
	
}
