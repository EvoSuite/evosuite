/**
 * 
 */
package de.unisb.cs.st.evosuite.ma;

import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.evosuite.testcase.TestCase;

/**
 * @author Yury Pavlov
 * 
 */
public class TestCaseTuple {
	TestCase testCase;
	Set<Integer> coverage = new HashSet<Integer>();

	/**
	 * Default constructor to create new TestCaseTupel
	 * 
	 * @param testCase
	 *            TestCase
	 * @param coverage
	 *            Set of Integer
	 */
	public TestCaseTuple(TestCase testCase, Set<Integer> coverage) {
		this.testCase = testCase;
		this.coverage = coverage;
	}

	/**
	 * @return the coverage
	 */
	public Set<Integer> getCoverage() {
		return coverage;
	}

	/**
	 * @return the testCase
	 */
	public TestCase getTestCase() {
		return testCase;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		return false;
	}

}
