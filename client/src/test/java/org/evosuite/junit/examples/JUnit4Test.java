package org.evosuite.junit.examples;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category({ SlowTests.class, FastTests.class })
public class JUnit4Test {

	@Test
	public void foo() {
		
	}

	@Test
	@Category(SlowTests.class)
	public void bar() {
		
	}
}
