package org.evosuite.runtime;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;

import org.junit.Assume;
import org.junit.Test;

public class GuiSupportTest {

	//only one of the 2 tests can be actually executed, as dependent on JVM options
	
	@Test
	public void testWhenHeadless(){
		Assume.assumeTrue(GraphicsEnvironment.isHeadless());
		
		GuiSupport.setHeadless(); //should do nothing
		Assert.assertTrue(GraphicsEnvironment.isHeadless());
		
		GuiSupport.restoreHeadlessMode(); //should do nothing
		Assert.assertTrue(GraphicsEnvironment.isHeadless());		
	}
	
	@Test
	public void testWhenNotHeadless(){
		Assume.assumeTrue(! GraphicsEnvironment.isHeadless());
		
		GuiSupport.setHeadless(); 
		Assert.assertTrue(GraphicsEnvironment.isHeadless());
		
		GuiSupport.restoreHeadlessMode(); //should restore headless
		Assert.assertTrue(! GraphicsEnvironment.isHeadless());		
	}
}
