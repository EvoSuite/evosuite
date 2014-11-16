package org.evosuite.runtime;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

/**
 * Class used to handle some particular behaviors of GUI components in the
 * generated JUnit test files
 * 
 * @author arcuri
 *
 */
public class GuiSupport {

	
	/**
	 * Where the tests run in headless mode?
	 */
	private static final boolean isDefaultHeadless = GraphicsEnvironment.isHeadless();
	
	/**
	 * Set the JVM in headless mode
	 */
	public static void setHeadless(){
		
		if(isDefaultHeadless){
			//already headless: nothing to do
			return;
		}
		
		setHeadless(true);
	}

	public static void initialize(){
		/*
		 * Force the loading of fonts.
		 * This is needed because font loading in the JVM can take several seconds (done only once),
		 * and that can mess up the JUnit test execution timeouts...   
		 */
		(new javax.swing.JButton()).getFontMetrics(new java.awt.Font(null));
	}
	

	
	/**
	 *  Restore the original headless setting of when the JVM was started.
	 *  This is necessary for when EvoSuite tests (which are in headless mode) are
	 *  run together with manual tests that are not headless. 
	 */
	public static void restoreHeadlessMode(){
		if(GraphicsEnvironment.isHeadless() && !isDefaultHeadless){
			setHeadless(false);
		}
	}
	
	
	private static void setHeadless(boolean isHeadless){
		
		//changing system property is not enough
		java.lang.System.setProperty("java.awt.headless", ""+isHeadless);
		
		Field headless; // need reflection
		try {
			//AWT classes check GraphicsEnvironment for headless state
			headless = java.awt.GraphicsEnvironment.class.getDeclaredField("headless");
			headless.setAccessible(true);
			headless.set(null, (Boolean) isHeadless);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			//this should never happen. if it doesn't work, then all GUI tests would be messed up :( 
			throw new RuntimeException("ERROR: failed to change AWT Headless state: "+e.getMessage(),e);
		}		
	}
}
