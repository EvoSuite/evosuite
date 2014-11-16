package com.examples.with.different.packagename.testcarver;

import org.junit.Test;

public class PrimitivesTest {

	@Test
	public void test() {
		ObjectWrapper wrapper = new ObjectWrapper();

		int zero = 0;
		Integer one = 1;
		char two = '2';
		float three = 3f;
		double four = 4d;
		long five = 5l;
		byte six = 6;
		String seven = "7";

		String s = "" + zero + one + two + three + four + five + six + seven;

		wrapper.set(s);
		wrapper.set(zero);
		wrapper.set(one);
		wrapper.set(two);
		wrapper.set(three);
		wrapper.set(four);
		wrapper.set(five);
		wrapper.set(six);
		wrapper.set(seven);
	}
}
