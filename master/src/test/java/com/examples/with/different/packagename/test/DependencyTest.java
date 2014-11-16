package com.examples.with.different.packagename.test;

import com.examples.with.different.packagename.Dummy;

public class DependencyTest {

	public void testMe(Dummy dummy) {
		if (dummy.isDummy())
			System.out.println("Juhu!");
	}
}
