package org.evosuite.setup;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class TestClusterUtils {

	public static boolean isAnonymousClass(String className) {
		int pos = className.lastIndexOf('$');
		if(pos < 0)
			return false;
		char firstLetter = className.charAt(pos + 1);
		if(firstLetter >= '0' && firstLetter <= '9')
			return true;

		return false;
	}

	public static void makeAccessible(Field field) {
		if (!Modifier.isPublic(field.getModifiers())
		        || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
			field.setAccessible(true);
		}
	}

	public static void makeAccessible(Method method) {
		if (!Modifier.isPublic(method.getModifiers())
		        || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
			method.setAccessible(true);
		}
	}

	public static void makeAccessible(Constructor<?> constructor) {
		if (!Modifier.isPublic(constructor.getModifiers())
		        || !Modifier.isPublic(constructor.getDeclaringClass().getModifiers())) {
			constructor.setAccessible(true);
		}
	}
}
