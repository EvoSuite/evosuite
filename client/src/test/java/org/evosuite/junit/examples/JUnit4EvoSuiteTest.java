package org.evosuite.junit.examples;

import org.evosuite.annotations.EvoSuiteTest;
import org.junit.experimental.categories.Category;

@Category({ SlowTests.class, FastTests.class })
public class JUnit4EvoSuiteTest {


	@EvoSuiteTest
	public void foo2() {

	}

}
