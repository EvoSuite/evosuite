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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ConcolicReflection {

	private final int value;

	public ConcolicReflection() {
		value = 10;
	}

	public int getValue() {
		return value;
	}

	public static Object classNewInstance(int x)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = ConcolicReflection.class;
		Object newObject = clazz.newInstance();
		if (x != 10) {
			return null;
		} else {
			return newObject;
		}
	}

	public static Object newInstanceNoReflection(int x) {
		Object newObject = new ConcolicReflection();
		if (x != 10) {
			return null;
		} else {
			return newObject;
		}
	}

	public static Object objClassNewInstance(int x)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = Object.class;
		Object newObject = clazz.newInstance();
		if (x != 10) {
			return null;
		} else {
			return newObject;
		}
	}

	public static Object constructorNewInstance(int x) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = ConcolicReflection.class;
		Constructor<?> ctor = clazz.getConstructor();
		Object newObject = ctor.newInstance();
		if (x != 10) {
			return null;
		} else {
			return newObject;
		}
	}

	public boolean greaterThanZero(Integer x) {
		int intValue = x.intValue();
		if (intValue > 0) {
			return true;
		} else {
			return false;
		}
	}

	public static Object methodInvoke(int x) throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {
		ConcolicReflection o = new ConcolicReflection();
		Class<?> clazz = o.getClass();
		Method method = clazz.getMethod("greaterThanZero", Integer.class);
		Object retObject = method.invoke(o, x);
		if (retObject.equals(Boolean.TRUE)) {
			return o;
		} else {
			return null;
		}
	}

	public static Object objConstructorNewInstance(int x) throws NoSuchMethodException, SecurityException,
			InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = Object.class;
		Constructor<?> ctor = clazz.getConstructor();
		Object newObject = ctor.newInstance();
		if (x != 10) {
			return null;
		} else {
			return newObject;
		}
	}
}
