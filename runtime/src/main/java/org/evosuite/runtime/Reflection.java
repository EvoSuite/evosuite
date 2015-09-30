/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.runtime;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.evosuite.runtime.instrumentation.InstrumentedClass;
import org.evosuite.runtime.instrumentation.RemoveFinalClassAdapter;
import org.mockito.asm.Opcodes;

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

	public static Class<?>[] getInterfaces(Class<?> clazz) throws SecurityException {
		return sortArrayInPlace(Arrays.stream(clazz.getInterfaces()).filter(c -> !c.equals(InstrumentedClass.class)).toArray(Class[]::new));
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
	
	public static int getModifiers(Class<?> clazz) {
		int modifier = clazz.getModifiers();
		if(RemoveFinalClassAdapter.finalClasses.contains(clazz.getCanonicalName())) {
			modifier = modifier | Opcodes.ACC_FINAL;
		}
		return modifier;
	}

}
