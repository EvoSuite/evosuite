package org.evosuite.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class StaticFieldReset {

	private static final StaticFieldReset instance = new StaticFieldReset();

	public static StaticFieldReset getInstance() {
		return instance;
	}

	private List<String> classesForStaticReset = new LinkedList<String>();

	public void resetStaticFields(String classNameWithDots) {
		try {
			Class<?> clazz = Class.forName(classNameWithDots);
			Method m = clazz.getMethod("__STATIC_RESET", (Class<?>[]) null);
			m.invoke(null, (Object[]) null);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}



}
