package com.examples.with.different.packagename.testcarver;

import org.junit.Assert;
import org.junit.Test;

public class SimpleTest {

	@Test
	public void actuallTest() {
		Simple sim = new Simple();

		boolean b0 = sim.incr();
		Assert.assertFalse(b0);

		boolean b1 = sim.sameValues(2, 4);
		Assert.assertFalse(b1);

		boolean b2 = sim.sameValues(5, 5);
		Assert.assertTrue(b2);
	}

	@SuppressWarnings("unused")
	public void thisIsNotATest() {
		Simple sim = new Simple();
	}
}
