package com.examples.with.different.packagename;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class TestBMICalculator {

	@Test
	public void testConstructor() throws Throwable {
		assertNotNull(new BMICalculator());
	}

	@Test
	public void testVeryObese() throws Throwable {
		String string0 = BMICalculator.calculateBMICategory((-1.0), 2138.41);
		assertEquals("very obese", string0);
	}

	@Test
	public void testUnderweight() throws Throwable {
		String string0 = BMICalculator.calculateBMICategory(2010.42781, 40.0);
		assertEquals("underweight", string0);
	}

	@Test
	public void testOverweight() throws Throwable {
		String string0 = BMICalculator.calculateBMICategory((-1.0), 25.0);
		assertEquals("overweight", string0);
	}

	@Test
	public void testObese() throws Throwable {
		String string0 = BMICalculator.calculateBMICategory((-1.0), 30.0);
		assertEquals("obese", string0);
	}

	@Test
	public void testHealthy() throws Throwable {
		String string0 = BMICalculator.calculateBMICategory((-1.0), 18.5);
		assertEquals("healthy", string0);
	}
}
