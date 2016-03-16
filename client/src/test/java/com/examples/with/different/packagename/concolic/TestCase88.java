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
package com.examples.with.different.packagename.concolic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class TestCase88 {

	public int callbackMethodToIgnore(int left, int right) {
		int counter = 0;
		for (int i = 0; i <= right; i++) {
			counter += left;
		}
		return counter;
	}

	public static void test(int int0, int int1) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {

		TestCase88 instance0 = new TestCase88();
		Method method = TestCase88.class.getMethod("callbackMethodToIgnore",
				int.class, int.class);

		Object ret = method.invoke(instance0, int0, int1);
		int int2 = (Integer) ret;
		int int3 = 210;
		
		Assertions.checkEquals(int2, int3);
	}

}
