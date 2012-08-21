package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase76 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int int0 = ConcolicMarker.mark(10, "int0");
		int int1 = ConcolicMarker.mark(0, "int1");
		int int2 = ConcolicMarker.mark(-1, "int2");
		int[] array = new int[int0];
		array[int1] = int2;
		array[1] = int2;
		checkEquals(array.length, 10);

	}

}
