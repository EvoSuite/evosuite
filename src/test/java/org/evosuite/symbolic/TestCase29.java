package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase29 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		{
			boolean boolean0 = ConcolicMarker.mark(true, "boolean0");
			boolean boolean1 = true;
			checkEquals(boolean0, boolean1);
		}
		{
			boolean boolean2 = ConcolicMarker.mark(false, "boolean2");
			boolean boolean3 = false;
			checkEquals(boolean2, boolean3);
		}

	}

}
