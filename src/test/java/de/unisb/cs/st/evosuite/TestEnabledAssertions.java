package de.unisb.cs.st.evosuite;

import org.junit.*;

/**
 * @author Andrea Arcuri
 * 
 */
public class TestEnabledAssertions {

	/*
	 * when we run the test cases in series, we need to be reminded that we should activate 
	 * the assertions inside EvoSuite with -ea:de.unisb.cs.st...
	 */
	@Test
	public void testIfAssertionsAreEnabled(){
		boolean enabled = false;
		
		assert (enabled=true)  ; 
		
		Assert.assertTrue(enabled);
	}
	
}
