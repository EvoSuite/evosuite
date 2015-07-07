package org.evosuite.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.evosuite.junit.examples.AbstractJUnit3Test;
import org.evosuite.junit.examples.JUnit3Suite;
import org.evosuite.junit.examples.JUnit3Test;
import org.evosuite.junit.examples.JUnit4Categories;
import org.evosuite.junit.examples.JUnit4Suite;
import org.evosuite.junit.examples.JUnit4Test;
import org.evosuite.junit.examples.Not_A_Test;
import org.evosuite.junit.examples.JUnit4ParameterizedTest;
import org.junit.Test;

public class CoverageAnalysisTest {

	@Test
	public void isTest() {
		assertFalse(CoverageAnalysis.isTest(Not_A_Test.class));

		assertTrue(CoverageAnalysis.isTest(JUnit3Test.class));
		assertFalse(CoverageAnalysis.isTest(JUnit3Suite.class));
		assertFalse(CoverageAnalysis.isTest(AbstractJUnit3Test.class));

		assertTrue(CoverageAnalysis.isTest(JUnit4Test.class));
		assertFalse(CoverageAnalysis.isTest(JUnit4Suite.class));
		assertFalse(CoverageAnalysis.isTest(JUnit4Categories.class));
		assertTrue(CoverageAnalysis.isTest(JUnit4ParameterizedTest.class));
	}
}
