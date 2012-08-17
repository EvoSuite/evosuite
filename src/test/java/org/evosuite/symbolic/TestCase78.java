package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase78 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int int0 = ConcolicMarker.mark(10, "int0");
		Integer integer0 = Integer.valueOf(int0);
		int int1 = integer0.intValue();
		checkEquals(int1, 10);

	}

}
