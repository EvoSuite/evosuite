package org.evosuite.symbolic;

import org.evosuite.symbolic.dsc.ConcolicMarker;
import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase62 {

	public static void main(String[] args) {
		String string1 = "Togliere sta roba";
		String string0 = ConcolicMarker.mark(string1, "string0");

		int catchCount = 0;

		try {
			string0.lastIndexOf(212, -1);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			string0.lastIndexOf(212, Integer.MAX_VALUE);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			string0.lastIndexOf(null);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			String nullStringRef = null;
			string0.lastIndexOf(nullStringRef, 0);
		} catch (NullPointerException ex) {
			catchCount++;
		}
		
		checkEquals(2,catchCount);
		
		int int0 = string0.lastIndexOf((int)'a');
		int int1 = string0.lastIndexOf((int)'a',string0.length()-1);
		int int2 = string0.lastIndexOf("a");
		int int3 = string0.lastIndexOf("a",string0.length()-1);
		int int4 = string1.lastIndexOf("a");
		
		checkEquals(int0,int4);
		checkEquals(int1,int4);
		checkEquals(int2,int4);
		checkEquals(int3,int4);
	}
}
