package com.examples.with.different.packagename.concolic;

public class StringHandler {

	private String str;

	public StringHandler(String str) {
		this.str = str;
	}

	public boolean equals(String otherString) {
		return this.str.equals(otherString);
	}

	public void toUpperCase() {
		str = str.toUpperCase();
	}

	public static boolean stringMatches(String string, String regex) {
		return string.matches(regex);
	}

	public static void checkEquals(boolean l, boolean r) {
		if (l != r) {
			throw new RuntimeException();
		}

	}
}