package org.evosuite.coverage.epa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.evosuite.epa.EpaAction;
import org.evosuite.epa.EpaActionPrecondition;
import org.evosuite.testcase.execution.EvosuiteError;

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

	/**
	 * Returns true if the method has the @EpaAction annotation.
	 * 
	 * @param method
	 * @return
	 */
	private static Annotation getEpaActionAnnotation(Executable method) {
		return getAnnotation(method, EpaAction.class);
	}

	/**
	 * Returns true if the method has the @EpaActionPrecondition annotation.
	 * 
	 * @param method
	 * @return
	 */
	private static Annotation getEpaActionPreconditionAnnotation(Method method) {
		return getAnnotation(method, EpaActionPrecondition.class);
	}

	private static Annotation getAnnotation(Executable executable, Class<?> annotationClass) {
		for (Annotation annotation : executable.getDeclaredAnnotations()) {
			if (annotation.annotationType().getName().equals(annotationClass.getName())) {
				return annotation;
			}
		}
		return null;
	}

	/**
	 * Returns the value of the field "name" of the annotation @EpaAction
	 * 
	 * @param annotation
	 *            an @EpaAction annotation
	 * 
	 * @return
	 * @throws EvosuiteError
	 */
	private static String getEpaActionAnnotationName(Annotation annotation) throws EvosuiteError {
		try {
			final Method nameMethod = annotation.getClass().getDeclaredMethod("name");
			Object idResult = nameMethod.invoke(annotation);
			if (idResult.getClass() == String.class) {
				final String actionId = (String) idResult;
				return actionId;
			} else {
				return null;
			}
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			throw new EvosuiteError(e);
		}
	}

	/**
	 * Collects a maps from actionId to the methods that are labelled with
	 * a @EpaAction annotation
	 * 
	 * @param objectClass
	 * @return
	 */
	public static Map<String, Set<Method>> getEpaActionMethods(Class<?> objectClass) {
		Map<String, Set<Method>> epaActionMethodsMap = new HashMap<String, Set<Method>>();
		Class<?> currentClass = objectClass;
		while (currentClass != null) {
			for (Method method : currentClass.getDeclaredMethods()) {
				Annotation epaActionAnnotation = getEpaActionAnnotation(method);
				if (epaActionAnnotation != null) {
					String actionId = getEpaActionAnnotationName(epaActionAnnotation);
					if (actionId == null) {
						throw new EvosuiteError("name field was not found in an @EpaAction annotation");
					}
					if (!epaActionMethodsMap.containsKey(actionId)) {
						epaActionMethodsMap.put(actionId, new HashSet<Method>());
					}
					epaActionMethodsMap.get(actionId).add(method);
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		return epaActionMethodsMap;
	}

	public static Map<String, Set<Constructor<?>>> getEpaActionConstructors(Class<?> objectClass) {
		Map<String, Set<Constructor<?>>> epaActionMethodsMap = new HashMap<String, Set<Constructor<?>>>();
		Class<?> currentClass = objectClass;
		while (currentClass != null) {
			for (Constructor<?> constructor : currentClass.getDeclaredConstructors()) {
				Annotation epaActionAnnotation = getEpaActionAnnotation(constructor);
				if (epaActionAnnotation != null) {
					String actionId = getEpaActionAnnotationName(epaActionAnnotation);
					if (actionId == null) {
						throw new EvosuiteError("name field was not found in an @EpaAction annotation");
					}
					if (!epaActionMethodsMap.containsKey(actionId)) {
						epaActionMethodsMap.put(actionId, new HashSet<Constructor<?>>());
					}
					epaActionMethodsMap.get(actionId).add(constructor);
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		return epaActionMethodsMap;
	}

	/**
	 * Collects a maps from actionId to each @EpaActionPrecondition method
	 * 
	 * @param objectClass
	 * @return
	 */
	public static Map<String, Method> getEpaActionPreconditionMethods(Class<?> objectClass) {
		Map<String, Method> epaActionPreconditionMethodsMap = new HashMap<String, Method>();
		Class<?> currentClass = objectClass;
		while (currentClass != null) {
			for (Method method : currentClass.getDeclaredMethods()) {
				Annotation epaActionPreconditionAnnotation = getEpaActionPreconditionAnnotation(method);
				if (epaActionPreconditionAnnotation != null) {
					String actionId = getEpaActionAnnotationName(epaActionPreconditionAnnotation);
					if (actionId == null) {
						throw new EvosuiteError("name field was not found in an @EpaAction annotation");
					}
					if (epaActionPreconditionMethodsMap.containsKey(actionId)) {
						throw new EvosuiteError("Found repeated precondition methods for action " + actionId);
					}
					epaActionPreconditionMethodsMap.put(actionId, method);
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		return epaActionPreconditionMethodsMap;
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
