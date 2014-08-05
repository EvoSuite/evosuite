package com.examples.with.different.packagename.testcarver;

import org.junit.Test;

public class DifficultClassTest {

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
	}
}
