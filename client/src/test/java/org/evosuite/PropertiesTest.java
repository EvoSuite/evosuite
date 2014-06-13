package org.evosuite;

import static org.junit.Assert.fail;

import org.evosuite.Properties.NoSuchParameterException;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class PropertiesTest {

	@BeforeClass
	public static void initClass(){
		Properties.getInstance();
	}
	
	@After
	public void reset(){
		Properties.getInstance().resetToDefaults();
	}
	
	@Test
	public void testParameterThatDoesNotExist(){
		try {
			Properties.getInstance().setValue("a_parameter_that_does_not_exist", 1);
			fail();
		} catch (IllegalArgumentException | IllegalAccessException
				| NoSuchParameterException e) {
			//expected
		}
	}
	
	@Test
	public void testOutOfRangeInput() throws IllegalArgumentException, IllegalAccessException, NoSuchParameterException{
		
		Properties.getInstance().setValue("crossover_rate", 0.6); //this should be OK
		
		try{
			Properties.getInstance().setValue("crossover_rate", 2.5);
			fail();
		} catch(Exception e){/* OK*/}

		try{
			Properties.getInstance().setValue("crossover_rate", -10.6);
			fail();
		} catch(Exception e){/* OK*/}
	}
	
	@Test
	public void testInvalidBooleanInput() throws Exception{
		
		final boolean defaultValue = Properties.TEST_CARVING;
		
		boolean value = Properties.getBooleanValue("test_carving");
		Assert.assertEquals(defaultValue, value);
		
		Properties.getInstance().setValue("test_carving", !defaultValue);
		value = Properties.getBooleanValue("test_carving");
		Assert.assertNotEquals(defaultValue, value);
		
		try{
			Properties.getInstance().setValue("test_carving", "tru");
			fail();
		} catch(Exception e){
			//expected
		}
	}
	
	@Test
	public void testReset(){
		
		final String defaultValue = Properties.TARGET_CLASS;
		
		final String aString = "foo_foo_foo";
		Assert.assertNotEquals(defaultValue, aString);
				
		Properties.TARGET_CLASS = aString;
		Assert.assertEquals(aString,Properties.TARGET_CLASS);
		
		Properties.getInstance().resetToDefaults();
		Assert.assertEquals(defaultValue,Properties.TARGET_CLASS);
	}
}
