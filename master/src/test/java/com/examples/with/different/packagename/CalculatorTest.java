package com.examples.with.different.packagename;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.examples.with.different.packagename.Calculator;

public class CalculatorTest {

	@Test
	public void testMul() {
		assertTrue(Calculator.mul(1, 2) == 2);
	}

	@Test
	public void testDiv() {
		assertTrue(Calculator.div(4, 2) == 2);
	}

	@Test
	public void testAdd() {
		assertTrue(Calculator.add(1, 2) == 3);
	}

	@Test
	public void testSub() {
		assertTrue(Calculator.sub(2, 1) == 1);
	}
}
