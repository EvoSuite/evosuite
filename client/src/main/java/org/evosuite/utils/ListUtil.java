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

import java.util.*;
public abstract class ListUtil {
	/**
	 * <p>tail</p>
	 *
	 * @param list a {@link java.util.List} object.
	 * @param <T> a T object.
	 * @return a {@link java.util.List} object.
	 */
	public static <T> List<T> tail(List<T> list) {
		return list.subList(1, list.size());
	}

	/**
	 * <p>anyEquals</p>
	 *
	 * @param list a {@link java.util.List} object.
	 * @param obj a T object.
	 * @param <T> a T object.
	 * @return a boolean.
	 */
	public static <T> boolean anyEquals(List<T> list, T obj) {
		for (T item : list) {
			if (item.equals(obj)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * <p>shuffledList</p>
	 *
	 * @param list a {@link java.util.List} object.
	 * @param <T> a T object.
	 * @return a {@link java.util.List} object.
	 */
	public static <T> List<T> shuffledList(List<T> list) {
		ArrayList<T> result = new ArrayList<T>(list);
		Collections.shuffle(result);
		return result;
	}

	/**
	 * <p>shuffledList</p>
	 *
	 * @param list a {@link java.util.List} object.
	 * @param rnd a {@link java.util.Random} object.
	 * @param <T> a T object.
	 * @return a {@link java.util.List} object.
	 */
	public static <T> List<T> shuffledList(List<T> list, Random rnd) {
		ArrayList<T> result = new ArrayList<T>(list);
		Collections.shuffle(result, rnd);
		return result;
	}
}
