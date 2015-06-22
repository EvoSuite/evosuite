package org.evosuite.junit.examples;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  JUnit3Test.class,
  JUnit4Test.class
})
public class JUnit4Suite {
	// the class remains empty,
	// used only as a holder for the above annotations
}
