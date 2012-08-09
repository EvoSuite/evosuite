package org.evosuite.symbolic;

import java.util.regex.PatternSyntaxException;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase67 {

	public static void main(String[] args) {
		String string0 = "Togliere sta roba";
		String string1 = ConcolicMarker.mark(string0, "string0");

		int catchCount = 0;

		try {
			string1.regionMatches(true, 0, null, 0, 0);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			string1.regionMatches(true, -1, "sto", 0, 0);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			string1.regionMatches(true, 0, "sto", -1, 0);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			string1.regionMatches(true, 0, "sto", 0, -1);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			string1.regionMatches(true, 0, "sto", 0, Integer.MAX_VALUE);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		checkEquals(1, catchCount);

		boolean boolean0 = string1.regionMatches(true, 0, "sto", 0,
				Integer.MAX_VALUE);
		boolean boolean1 = string0.regionMatches(true, 0, "sto", 0,
				Integer.MAX_VALUE);

		checkEquals(boolean1, boolean0);

	}
}
