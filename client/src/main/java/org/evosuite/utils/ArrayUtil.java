/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Gordon Fraser
 */
package org.evosuite.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
public abstract class ArrayUtil {
	/**
	 * <p>asSet</p>
	 *
	 * @param values a T object.
	 * @param <T> a T object.
	 * @return a {@link java.util.Set} object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Set<T> asSet(T... values) {
		return new HashSet<T>(Arrays.asList(values));
	}

	/** Constant <code>DEFAULT_JOIN_SEPARATOR="IterUtil.DEFAULT_JOIN_SEPARATOR"</code> */
	public static final String DEFAULT_JOIN_SEPARATOR = IterUtil.DEFAULT_JOIN_SEPARATOR;

	/**
	 * <p>box</p>
	 *
	 * @param array an array of int.
	 * @return an array of {@link java.lang.Integer} objects.
	 */
	public static Integer[] box(int[] array) {
		Integer[] result = new Integer[array.length];

		/* Can't use System.arraycopy() -- it doesn't do boxing */
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}

		return result;
	}

	/**
	 * <p>box</p>
	 *
	 * @param array an array of long.
	 * @return an array of {@link java.lang.Long} objects.
	 */
	public static Long[] box(long[] array) {
		Long[] result = new Long[array.length];

		/* Can't use System.arraycopy() -- it doesn't do boxing */
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}

		return result;
	}

	/**
	 * <p>box</p>
	 *
	 * @param array an array of byte.
	 * @return an array of {@link java.lang.Byte} objects.
	 */
	public static Byte[] box(byte[] array) {
		Byte[] result = new Byte[array.length];

		/* Can't use System.arraycopy() -- it doesn't do boxing */
		for (int i = 0; i < array.length; i++) {
			result[i] = array[i];
		}

		return result;
	}

	/**
	 * <p>join</p>
	 *
	 * @param array an array of {@link java.lang.Object} objects.
	 * @param separator a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String join(Object[] array, String separator) {
		return StringUtils.join(array, separator);
	}

	/**
	 * <p>join</p>
	 *
	 * @param array an array of {@link java.lang.Object} objects.
	 * @return a {@link java.lang.String} object.
	 */
	public static String join(Object[] array) {
		return StringUtils.join(array, DEFAULT_JOIN_SEPARATOR);
	}

	/**
	 * <p>join</p>
	 *
	 * @param array an array of long.
	 * @param separator a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String join(long[] array, String separator) {
		return join(box(array), separator);
	}

	/**
	 * <p>join</p>
	 *
	 * @param array an array of long.
	 * @return a {@link java.lang.String} object.
	 */
	public static String join(long[] array) {
		return join(array, DEFAULT_JOIN_SEPARATOR);
	}

	/**
	 * <p>join</p>
	 *
	 * @param array an array of byte.
	 * @param separator a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String join(byte[] array, String separator) {
		return join(box(array), separator);
	}

	/**
	 * <p>join</p>
	 *
	 * @param array an array of byte.
	 * @return a {@link java.lang.String} object.
	 */
	public static String join(byte[] array) {
		return join(array, DEFAULT_JOIN_SEPARATOR);
	}

	/**
	 * <p>contains</p>
	 * 
	 * @param array
	 * @param object
	 * @return true if array contains an instance equals to object
	 */
	public static boolean contains(Object[] array, Object object) {
	    for (Object obj : array) {
	    	if (object instanceof String && obj.toString().equals(object))
		        return true;
	    	else if (obj.equals(object))
	            return true;
	    }
	    return false;
	}

	/**
	 * 
	 * @param arr
	 * @param obj
	 * @return
	 */
	public static Object[] append(Object[] arr, Object obj) {
	    final int N = arr.length;
	    arr = Arrays.copyOf(arr, N + 1);
	    arr[N] = obj;
	    return arr;
	}
}
