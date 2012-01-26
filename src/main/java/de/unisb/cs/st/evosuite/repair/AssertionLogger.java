/**
 * 
 */
package de.unisb.cs.st.evosuite.repair;

/**
 * @author Gordon Fraser
 * 
 */
public class AssertionLogger {

	// Added preliminary implementation
	
	public static void assertEquals(String className, int testId, int assertionId,
	        double expected, double actual, double delta) {
		if (Math.abs(expected - actual) < delta) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertEquals(String className, int testId, int assertionId,
	        long expected, long actual) {
		if (expected == actual) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertEquals(String className, int testId, int assertionId,
	        java.lang.Object expected, java.lang.Object actual) {
		if (expected.equals(actual)) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertFalse(String className, int testId, int assertionId,
	        boolean condition) {
		if (!condition) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertNotNull(String className, int testId, int assertionId,
	        java.lang.Object object) {
		if (object != null) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertNull(String className, int testId, int assertionId,
	        java.lang.Object object) {
		if (object == null) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void assertTrue(String className, int testId, int assertionId,
	        boolean condition) {
		if (condition) {
			System.out.println("Assertion Violated: " + className + ":" + testId + ":" + assertionId);
		}
	}

	public static void fail() {
		System.out.println("Fail");
	}

	public static void fail(String message) {
		System.out.println("Fail: " + message);
	}
}
