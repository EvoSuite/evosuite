package com.examples.with.different.packagename.concolic;


import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase61 {

//	String string0 = ConcolicMarker.mark("Togliere sta roba", "string0");
	public static void test(String string0) {
		String string1 = "Togliere sta roba";

		int catchCount = 0;

		try {
			string0.indexOf(212, -1);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			string0.indexOf(212, Integer.MAX_VALUE);
		} catch (StringIndexOutOfBoundsException ex) {
			catchCount++;
		}

		try {
			string0.indexOf(null);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			String nullStringRef = null;
			string0.indexOf(nullStringRef, 0);
		} catch (NullPointerException ex) {
			catchCount++;
		}
		
		checkEquals(2,catchCount);
		
		int int0 = string0.indexOf((int)'a');
		int int1 = string0.indexOf((int)'a',5);
		int int2 = string0.indexOf("a");
		int int3 = string0.indexOf("a",5);
		int int4 = string1.indexOf("a");
		
		checkEquals(int0,int4);
		checkEquals(int1,int4);
		checkEquals(int2,int4);
		checkEquals(int3,int4);
	}
}
