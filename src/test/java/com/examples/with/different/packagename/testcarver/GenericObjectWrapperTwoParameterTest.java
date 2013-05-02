package com.examples.with.different.packagename.testcarver;

import org.junit.Assert;
import org.junit.Test;

public class GenericObjectWrapperTwoParameterTest {

	@Test
	public void test01() {
		GenericObjectWrapperTwoParameter<String, String> wrapper = new GenericObjectWrapperTwoParameter<String, String>();
		Assert.assertNull(wrapper.getValue());

		wrapper.setValue("Test");
		Assert.assertEquals("Test", wrapper.getValue());
		Assert.assertTrue(wrapper.isEqual("Test"));
		Assert.assertFalse(wrapper.isEqual("Not"));		
	}
	
}
