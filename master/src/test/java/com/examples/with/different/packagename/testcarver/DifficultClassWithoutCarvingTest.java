package com.examples.with.different.packagename.testcarver;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DifficultClassWithoutCarvingTest {

	@Test
	public void test() {
		DifficultDependencyClass dependency = new DifficultDependencyClass();
		dependency.inc();
		dependency.inc();
		dependency.inc();
		dependency.inc();
		dependency.inc();
		dependency.inc();
		dependency.inc();
		dependency.inc();
		dependency.inc();
		dependency.inc();
		DifficultClassWithoutCarving foo = new DifficultClassWithoutCarving();
		boolean result = foo.testMe(dependency);
		assertTrue(result);
	}
}
