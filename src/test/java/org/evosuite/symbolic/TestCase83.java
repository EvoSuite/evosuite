package org.evosuite.symbolic;

import java.util.regex.Pattern;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase83 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String regex = "a*b";
		String string0 = ConcolicMarker.mark("aaaaaab", "string0");
		boolean boolean0 = Pattern.matches(regex, string0);
		checkEquals(boolean0, true);

		String string1 = ConcolicMarker.mark("bbbb", "string1");
		boolean boolean1 = Pattern.matches(regex, string1);
		checkEquals(boolean1, false);

		StringBuffer stringBuffer0 = new StringBuffer("aaaaaab");
		boolean boolean2 = Pattern.matches(regex, stringBuffer0);
		checkEquals(boolean2, true);

		int catchCount = ConcolicMarker.mark(0, "catchCount");
		try {
			boolean boolean3 = Pattern.matches(regex, null);
			checkEquals(boolean3, false);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			boolean boolean3 = Pattern.matches(null, string0);
			checkEquals(boolean3, false);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			boolean boolean3 = Pattern.matches(null, null);
			checkEquals(boolean3, false);
		} catch (NullPointerException ex) {
			catchCount++;
		}
		checkEquals(catchCount, 3);
	}
}
