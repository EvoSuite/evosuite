package de.unisb.cs.st.evosuite.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public abstract class ArrayUtil {
	public static <T> Set<T> asSet(T... values) {
		return new HashSet<T>(Arrays.asList(values));
	}

	public static final String DEFAULT_JOIN_SEPARATOR = IterUtil.DEFAULT_JOIN_SEPARATOR;

	public static Object[] box(long[] array) {
		Object[] result = new Object[array.length];

		/* Can't use System.arraycopy() -- it doesn't do boxing */
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}

		return result;
	}

	public static Object[] box(byte[] array) {
		Object[] result = new Object[array.length];

		/* Can't use System.arraycopy() -- it doesn't do boxing */
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}

		return result;
	}

	public static String join(Object[] array, String separator) {
		return StringUtils.join(array, separator);
	}

	public static String join(Object[] array) {
		return StringUtils.join(array, DEFAULT_JOIN_SEPARATOR);
	}

	public static String join(long[] array, String separator) {
		return join(box(array), separator);
	}

	public static String join(long[] array) {
		return join(array, DEFAULT_JOIN_SEPARATOR);
	}

	public static String join(byte[] array, String separator) {
		return join(box(array), separator);
	}

	public static String join(byte[] array) {
		return join(array, DEFAULT_JOIN_SEPARATOR);
	}
}
