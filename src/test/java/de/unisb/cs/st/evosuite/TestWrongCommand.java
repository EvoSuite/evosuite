package de.unisb.cs.st.evosuite;

import org.junit.*;

/**
 * @author Andrea Arcuri
 * 
 */
public class TestWrongCommand {

	@Test
	public void testWrongCommand(){
		EvoSuite evosuite = new EvoSuite();
		
		Object result = evosuite.parseCommandLine(new String[]{"foo"});
		
		Assert.assertTrue( result == null);
	}
}
