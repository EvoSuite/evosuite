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
