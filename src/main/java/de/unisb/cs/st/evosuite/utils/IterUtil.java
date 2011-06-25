package de.unisb.cs.st.evosuite.utils;

public class IterUtil {
	public static final String DEFAULT_JOIN_SEPARATOR = ", ";

	public static String join(Iterable<?> iter, String separator) {
		StringBuilder result = new StringBuilder();
		boolean isFirst = true;
		
		for (Object item : iter) {
			if (!isFirst) {
				result.append(separator);
			}
			
			result.append(item);
			isFirst = false;
		}
		
		return result.toString();
	}
	
	public static String join(Iterable<?> iter) {
		return join(iter, DEFAULT_JOIN_SEPARATOR);
	}
}
