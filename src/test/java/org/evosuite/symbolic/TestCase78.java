package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase78 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int int0 = ConcolicMarker.mark(10, "int0");
		// box integer
		Integer integer0 = box(int0);
		// unbox integer
		int int1 = unbox(integer0);
		int int2 = 10;
		checkEquals(int1, int2);
	}

	public static Integer box(Integer i) {
		return i;
	}

	public static int unbox(int i) {
		return i;
	}
}
