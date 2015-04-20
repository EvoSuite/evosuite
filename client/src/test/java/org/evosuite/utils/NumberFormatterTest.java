package org.evosuite.utils;

import org.junit.*;

public class NumberFormatterTest {

	@Test
	public void testInfinity(){
		System.out.println(""+Double.POSITIVE_INFINITY);
		System.out.println(""+Double.NEGATIVE_INFINITY);
		
		double value = Double.NEGATIVE_INFINITY;
		String code = NumberFormatter.getNumberString(value);
		Assert.assertEquals("Double.NEGATIVE_INFINITY", code);
	}
	
}
