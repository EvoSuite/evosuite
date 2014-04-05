package org.evosuite.instrumentation;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestDoubleFloatComparison {

	@Test
	public void testInfinityAndDouble() {
		assertTrue(BooleanHelper.doubleSub(Double.POSITIVE_INFINITY, 0.0) != 0);
		assertTrue(BooleanHelper.doubleSub(Double.NEGATIVE_INFINITY, 0.0) != 0);
		assertTrue(BooleanHelper.doubleSub(0.0, Double.POSITIVE_INFINITY) != 0);
		assertTrue(BooleanHelper.doubleSub(0.0, Double.NEGATIVE_INFINITY) != 0);
	}

	@Test
	public void testNaNAndDouble() {
		assertTrue(BooleanHelper.doubleSub(Double.NaN, 0.0) != 0);
		assertTrue(BooleanHelper.doubleSub(0.0, Double.NaN) != 0);
	}
	
	@Test
	public void testInfinityAndFloat() {
		assertTrue(BooleanHelper.floatSub(Float.POSITIVE_INFINITY, 0.0F) != 0);
		assertTrue(BooleanHelper.floatSub(Float.NEGATIVE_INFINITY, 0.0F) != 0);
		assertTrue(BooleanHelper.floatSub(0.0F, Float.POSITIVE_INFINITY) != 0);
		assertTrue(BooleanHelper.floatSub(0.0F, Float.NEGATIVE_INFINITY) != 0);
	}

	@Test
	public void testNaNAndFloat() {
		assertTrue(BooleanHelper.floatSub(Float.NaN, 0.0F) != 0);
		assertTrue(BooleanHelper.floatSub(0.0F, Float.NaN) != 0);
	}

}
