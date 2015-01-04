package org.evosuite.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The content of arrays in reflection methods may differ between classloaders, therefore
 * we sort the output alphabetically
 * 
 * @author gordon
 *
 */
public class Reflection {

	private static <T> T[] sortArrayInPlace(T[] original) {
		List<T> methods = Arrays.asList(original);
		Collections.sort(methods, new Comparator<T>() {
			@Override
			public int compare(T o1, T o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		
		methods.toArray(original);
		return original;
	}
	
	public static Annotation[] getAnnotations(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(clazz.getAnnotations());
	}

	public static Class<?>[] getClasses(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(clazz.getClasses());
	}

	public static Method[] getMethods(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(clazz.getMethods());
	}

	public static Field[] getFields(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(clazz.getFields());
	}

	public static Constructor<?>[] getConstructors(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(clazz.getConstructors());
	}
	
	public static Annotation[] getDeclaredAnnotations(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(clazz.getDeclaredAnnotations());
	}
	
	public static Class<?>[] getDeclaredClasses(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(clazz.getDeclaredClasses());
	}

	public static Method[] getDeclaredMethods(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(clazz.getDeclaredMethods());
	}
	
	public static Field[] getDeclaredFields(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(clazz.getDeclaredFields());
	}

	public static Constructor<?>[] getDeclaredConstructors(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(clazz.getDeclaredConstructors());
	}

}
