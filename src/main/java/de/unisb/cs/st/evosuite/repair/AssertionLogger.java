/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.repair;

/**
 * @author Gordon Fraser
 * 
 */
public class AssertionLogger {
	
	public static boolean COVERAGE = false;

	// Added preliminary implementation
	
	public static void assertEquals(String className, int testId, int assertionId,
	        double expected, double actual, double delta) {
		if (Math.abs(expected - actual) < delta && COVERAGE) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertEquals(String className, int testId, int assertionId,
	        long expected, long actual) {
		if (expected == actual && COVERAGE) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertEquals(String className, int testId, int assertionId,
	        java.lang.Object expected, java.lang.Object actual) {
		if (expected.equals(actual) && COVERAGE) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertFalse(String className, int testId, int assertionId,
	        boolean condition) {
		if (!condition && COVERAGE) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertNotNull(String className, int testId, int assertionId,
	        java.lang.Object object) {
		if (object != null && COVERAGE) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertNull(String className, int testId, int assertionId,
	        java.lang.Object object) {
		if (object == null && COVERAGE) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertTrue(String className, int testId, int assertionId,
	        boolean condition) {
		if (condition && COVERAGE) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void fail() {
		if (COVERAGE) {
			System.out.println("Fail");			
		}
	}

	public static void fail(String message) {
		if (COVERAGE) {			
			System.out.println("Fail: " + message);
		}
	}
}
