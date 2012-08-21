package org.evosuite.symbolic;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase84 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String regex = "a*b";
		String string0 = ConcolicMarker.mark("aaaaaaaaaaab", "string0");

		Pattern pattern0 = Pattern.compile(regex);
		Matcher matcher0 = pattern0.matcher(string0);
		boolean boolean0 = matcher0.matches();

		checkEquals(boolean0, true);
	}

}
