package org.evosuite.symbolic;

public class Assertions {

	public static void checkEquals(long l, long r) {
		if (l != r) {
			throw new RuntimeException("check failed!");
		}
	}

	public static void checkEquals(float l, float r) {
		if (l != r) {
			throw new RuntimeException("check failed!");
		}
	}

	public static void checkEquals(int l, int r) {
		if (l != r) {
			throw new RuntimeException("check failed!");
		}
	}

	public static void checkEquals(byte l, byte r) {
		if (l != r) {
			throw new RuntimeException("check failed!");
		}
	}
	
	public static void checkEquals(double l, double r) {
		if (l != r) {
			throw new RuntimeException("check failed!");
		}
	}
	
	public static void checkEquals(boolean l, boolean r) {
		if (l != r) {
			throw new RuntimeException("check failed!");
		}
	}
	
	public static void checkNotEquals(boolean l, boolean r) {
		if (l == r) {
			throw new RuntimeException("check failed!");
		}
	}

	public static void checkEquals(char l, char r) {
		if (l != r) {
			throw new RuntimeException("check failed!");
		}
	}
	
	public static void checkObjectEquals(Object l, Object r) {
		if (l != r) {
			throw new RuntimeException("check failed!");
		}
	}

	public static void checkEquals(short l, short r) {
		if (l != r) {
			throw new RuntimeException("check failed!");
		}
	}
}
