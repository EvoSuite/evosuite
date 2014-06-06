package com.examples.with.different.packagename.concolic;

import java.util.regex.PatternSyntaxException;

import static org.evosuite.symbolic.Assertions.checkEquals;

public class TestCase65 {

	public static void test(String string1) {
		String string0 = "Togliere sta roba";

		int catchCount = 0;

		try {
			string1.replaceAll(null, string0);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			string1.replaceAll(string0, null);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			string1.replaceFirst(null, string0);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			string1.replaceFirst(string0, null);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			string1.replace(null, string0);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			string1.replace(string0, null);
		} catch (NullPointerException ex) {
			catchCount++;
		}

		try {
			string1.replaceAll("*", "s");
		} catch (PatternSyntaxException ex) {
			catchCount++;
		}

		checkEquals(7, catchCount);

		String string2 = string1.replace('a', 'b');
		String string3 = string0.replace('a', 'b');
		boolean boolean0 = string2.equals(string3);
		checkEquals(true, boolean0);

		String string4 = string1.replace("st", "roba");
		String string5 = string0.replace("st", "roba");
		boolean boolean1 = string4.equals(string5);
		checkEquals(true, boolean1);

		String string6 = string1.replaceAll("st", "roba");
		String string7 = string0.replaceAll("st", "roba");
		boolean boolean2 = string6.equals(string7);
		checkEquals(true, boolean2);

		String string8 = string1.replaceFirst("st", "roba");
		String string9 = string0.replaceFirst("st", "roba");
		boolean boolean3 = string8.equals(string9);
		checkEquals(true, boolean3);

	}
}
