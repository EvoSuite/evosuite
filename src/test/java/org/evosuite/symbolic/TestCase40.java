package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase40 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final int ARRAY_SIZE = 10;

		int int0 = ConcolicMarker.mark(ARRAY_SIZE, "int0");
		int int1 = ConcolicMarker.mark(1, "int1");
		float float0 = ConcolicMarker.mark(Float.POSITIVE_INFINITY, "float0");

		float[] floatArray0 = new float[int0];
		floatArray0[floatArray0.length - 1] = float0;
		float float1 = floatArray0[((int0 - int1) * 2 / 2)];

		checkEquals(float0, float1);

	}
}
