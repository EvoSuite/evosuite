package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;

public class TestCase33 {

	public static void main(String[] args) {
		String string0 = ConcolicMarker.mark("foo", "string0");
		bar(string0);
	}

	public static boolean bar(String s) {
		StringBuffer bf = new StringBuffer();
		bf.append('b');
		bf.append('a');
		bf.append('r');
		String bf_str = bf.toString();
		if (s.equals(bf_str)) {
			return true;
		} else {
			return false;
		}
	}
}
