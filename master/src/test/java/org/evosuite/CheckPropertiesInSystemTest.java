package org.evosuite;

import org.junit.Assert;

import org.junit.Test;

public class CheckPropertiesInSystemTest extends SystemTest {

	private static final String PROPERTY = "Some_property_name_used_for_testing_SystemTest";
	
	@Test
	public void setProperty(){
		Assert.assertNull(System.getProperty(PROPERTY));
		System.setProperty(PROPERTY, PROPERTY);
		Assert.assertNotNull(System.getProperty(PROPERTY));		
	}
	
	@Test
	public void getProperty(){
		Assert.assertNull(System.getProperty(PROPERTY));
	}
}
