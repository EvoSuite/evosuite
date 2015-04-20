package org.evosuite.runtime.mock;

/**
 * Class used to keep track of whether instrumented mock
 * class should use their mocked functionalities, or rather
 * roll back to the original behavior.
 * 
 * <p>
 * This is not really needed during the search.
 * But it is extremely important for the generated JUnit tests.
 * A typical problem happens when "manual" tests are executed
 * after EvoSuite generated ones: we do not want those manual
 * tests to use the mocked versions of the already loaded SUT
 * classes.
 * 
 * @author arcuri
 *
 */
public class MockFramework {

	private static volatile boolean active = false;
	
	/**
	 * If classes are mocked, then use the mock versions
	 * instead of the original
	 */
	public static void enable(){
		active = true;
	}
	
	public static void disable(){
		active = false;		
	}
	
	public static boolean isEnabled(){
		return active;
	}
}
