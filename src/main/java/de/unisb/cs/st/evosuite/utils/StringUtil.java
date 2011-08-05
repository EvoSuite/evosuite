package de.unisb.cs.st.evosuite.utils;

public abstract class StringUtil {
	public static String escapeQuotes(String str) {
		return str.replaceAll("['\"\\\\]", "\\\\$0");
	}
}
