package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase43 {

	public static void main(String[] args) {
		int int0 = ConcolicMarker.mark(Integer.MAX_VALUE, "var0");
		Integer integer0 = new Integer(int0);
		int int1 = integer0.intValue();
		int int2 = Integer.MAX_VALUE;
		checkEquals(int1, int2);

	}
}
