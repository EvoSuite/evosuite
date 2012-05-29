/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
