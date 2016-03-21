package org.evosuite.coverage.epa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * EPA-related utilities
 */
public class EPAUtils {

	public static boolean epaStateMethodExists(EPAState epaState, Class<?> objectClass) {
		try {
			getEpaStateMethod(epaState, objectClass);
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	public static Method getEpaStateMethod(EPAState epaState, Class<?> objectClass) throws NoSuchMethodException {
		Class<?> currentClass = objectClass;
		while (currentClass != null) {
			final Optional<Method> methodOptional = Arrays.stream(currentClass.getDeclaredMethods())
					.filter(declaredMethod -> methodIsAnnotatedAs(declaredMethod, "EpaState", epaState.getName()))
					.findAny();
			if (methodOptional.isPresent())
				return methodOptional.get();

			currentClass = currentClass.getSuperclass();
		}
		throw new NoSuchMethodException("Boolean query method for state " + epaState + " was not found in class "
				+ objectClass.getName() + " or any superclass");
	}

	public static Set<Method> getEpaActionMethods(String actionName, Class<?> objectClass) {
		Set<Method> methods = new HashSet<Method>();
		Class<?> currentClass = objectClass;
		while (currentClass != null) {
			final Set<Method> methodForClass = Arrays.stream(currentClass.getDeclaredMethods())
					.filter(declaredMethod -> methodIsAnnotatedAs(declaredMethod, "EpaAction", actionName))
					.collect(Collectors.toSet());
			methods.addAll(methodForClass);
			currentClass = currentClass.getSuperclass();
		}
		return methods;
	}

	private static boolean constructorIsAnnotatedAs(Constructor<?> constructor, String annotationName,
			String stateName) {
		Annotation[] declaredAnnotations = constructor.getDeclaredAnnotations();
		return Arrays.stream(declaredAnnotations)
				// Is annotation name the same?
				.filter(annotation -> {
					return annotation.annotationType().getSimpleName().equals(annotationName);
				})
				// Is ID the same?
				.filter(annotation -> {
					try {
						final Method idMethod = annotation.getClass().getDeclaredMethod("name");
						Object idResult = idMethod.invoke(annotation);
						if (idResult.getClass() == String.class) {
							final String idResultAsString = (String) idResult;
							if (idResultAsString.equals(stateName))
								return true;
						}
					} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
					}
					return false;
				}).findAny().isPresent();
	}

	private static boolean methodIsAnnotatedAs(Method method, String annotationName, String stateName) {
		Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
		return Arrays.stream(declaredAnnotations)
				// Is annotation name the same?
				.filter(annotation -> {
					return annotation.annotationType().getSimpleName().equals(annotationName);
				})
				// Is ID the same?
				.filter(annotation -> {
					try {
						final Method idMethod = annotation.getClass().getDeclaredMethod("name");
						Object idResult = idMethod.invoke(annotation);
						if (idResult.getClass() == String.class) {
							final String idResultAsString = (String) idResult;
							if (idResultAsString.equals(stateName))
								return true;
						}
					} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
					}
					return false;
				}).findAny().isPresent();
	}

	public static Set<Constructor<?>> getEpaActionConstructors(String actionName, Class<? extends Object> objectClass) {
		Set<Constructor<?>> constructors = new HashSet<Constructor<?>>();
		Class<?> currentClass = objectClass;
		while (currentClass != null) {
			final Set<Constructor<?>> methodForClass = Arrays.stream(currentClass.getDeclaredConstructors())
					.filter(declaredMethod -> constructorIsAnnotatedAs(declaredMethod, "EpaAction", actionName))
					.collect(Collectors.toSet());
			constructors.addAll(methodForClass);
			currentClass = currentClass.getSuperclass();
		}
		return constructors;
	}
}
