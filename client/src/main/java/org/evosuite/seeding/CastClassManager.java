/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.seeding;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;
import java.util.Map.Entry;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.ga.operators.selection.RankSelection;
import org.evosuite.instrumentation.InstrumentingClassLoader;
import org.evosuite.setup.*;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.gentyref.GenericTypeReflector;

import static java.util.Comparator.comparingInt;

public class CastClassManager {

	private static final Logger logger = LoggerFactory.getLogger(CastClassManager.class);
	private static CastClassManager instance = new CastClassManager();
	/**
	 * TODO:
	 */
	private final Map<GenericClass, Integer> classMap = new LinkedHashMap<>();
	private final List<Class<?>> specialCases =
			Arrays.asList(Comparable.class, Comparator.class, Iterable.class, Enum.class);

	// Private constructor due to singleton pattern, use getInstance() instead
	private CastClassManager() {
		initDefaultClasses();
	}

    public static CastClassManager getInstance() {
        return instance;
    }

	/**
	 * Sorts the classes contained in the given map by the number of generic type parameters and, if
	 * this number is equal, by their "priority" as given by the map. The rationale is that classes
	 * with less generic parameter types are easier to instantiate. Such classes will be at the
	 * front of the returned list. If the number of type parameters is equal for two classes they
	 * will be sorted based on the priority retrieved form the given map, such that classes with
	 * lower priority values proceed those with higher ones.
	 *
	 * @param classPriorityMap assigns classes to priorities
	 * @return list of classes ordered by number of generic type parameters, and by priority if tied
	 */
	public static List<GenericClass> sortByValue(Map<GenericClass, Integer> classPriorityMap) {
		final List<GenericClass> classes = new LinkedList<>(classPriorityMap.keySet());
		final Comparator<GenericClass> byNumParameters = comparingInt(GenericClass::getNumParameters);
		final Comparator<GenericClass> byPriority = comparingInt(classPriorityMap::get);
		classes.sort(byNumParameters.thenComparing(byPriority));
		return classes;
	}

	/**
	 * Chooses and returns a class among the given ones. Classes at the front of the list will
	 * have a higher chance of being chosen than those at the back.
	 *
	 * @param candidates list of candidates to choose from
	 * @return a candidate from the list of candidates
	 */
	public static GenericClass selectClass(List<GenericClass> candidates) {
		return candidates.get(RankSelection.getIdx(candidates));
	}

	/**
	 * Fills the class map with some default classes.
	 */
	private void initDefaultClasses() {
		classMap.put(new GenericClass(Object.class), 0);
		classMap.put(new GenericClass(String.class), 1);
		classMap.put(new GenericClass(Integer.class), 1);
	}

	public void addCastClass(String className, int depth) {
		final ClassLoader cl = TestGenerationContext.getInstance().getClassLoaderForSUT();
		final Class<?> clazz;
		try {
			clazz = cl.loadClass(className);
		} catch (ClassNotFoundException e) {
			// Ignore
			logger.debug("Error including cast class " + className + " because: " + e);
			return;
		}
		final GenericClass castClazz = new GenericClass(clazz);
		addCastClass(castClazz.getWithWildcardTypes(), depth);
	}

	public void addCastClass(Type type, int depth) {
		GenericClass castClazz = new GenericClass(type);
		addCastClass(castClazz.getWithWildcardTypes(), depth);
	}

	public void addCastClass(final GenericClass clazz, final int depth) {
		final Class<?> rawClass = clazz.getRawClass();

		if (rawClass == null) {
			logger.warn("ADDING NULL!");
			assert (false);
		}

		// If we have an abstract class, try to find concrete subclasses we can use instead.
		if (clazz.isAbstract()) {
			final ConcreteClassAnalyzer analyzer = ConcreteClassAnalyzer.getInstance();
			final InheritanceTree tree = TestCluster.getInheritanceTree();
			final Set<Class<?>> concreteClasses = analyzer.getConcreteClasses(rawClass, tree);

			for (final Class<?> concreteClass : concreteClasses) {
				final GenericClass c = new GenericClass(concreteClass);
				if (TestUsageChecker.canUse(c.getRawClass())) {
					classMap.put(c, depth);
				}
			}

			// If mocking is enabled, we can simply mock the abstract class in the generated tests.
			if (Properties.P_FUNCTIONAL_MOCKING > 0.0) {
				if (TestUsageChecker.canUse(rawClass)) {
					classMap.put(clazz, depth);
				}
			}
		} else if (TestUsageChecker.canUse(rawClass)) {
			classMap.put(clazz, depth);
		}
	}

	private void handleComparable() {
		// TODO
		throw new UnsupportedOperationException("not yet implemented");
	}

	private void handleComparator() {
		// TODO
		throw new UnsupportedOperationException("not yet implemented");
	}

	private void handleEnum() {
		// TODO
		throw new UnsupportedOperationException("not yet implemented");
	}

	private void handleIterable() {
		// TODO
		throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * True if this type variable is one of the java.* special cases
	 *
	 * @return
	 */
	private boolean isSpecialCase(TypeVariable<?> typeVariable) {
		return Arrays.stream(typeVariable.getBounds())
				.map(GenericTypeReflector::erase)
				.anyMatch(specialCases::contains);
	}

	/**
	 * True if this wildcard type is one of the java.* special cases
	 *
	 * @return
	 */
	private boolean isSpecialCase(WildcardType wildcardType) {
		// TODO
		throw new UnsupportedOperationException("not yet implemented");
	}

	private boolean addAssignableClass(
			final WildcardType wildcardType,
	        final Map<TypeVariable<?>, Type> typeMap) {
		final Set<Class<?>> classes = TestCluster.getInstance().getAnalyzedClasses();
		final Set<Class<?>> assignableClasses = new LinkedHashSet<>();

		for (final Class<?> clazz : classes) {
			if (!TestUsageChecker.canUse(clazz))
				continue;

			final GenericClass genericClass = new GenericClass(clazz).getWithWildcardTypes();
			if (!genericClass.satisfiesBoundaries(wildcardType, typeMap)) {
				logger.debug("Not assignable: " + clazz);
			} else {
				logger.debug("Assignable");
				assignableClasses.add(clazz);
			}
		}

		for (final Type t : typeMap.values()) {
			if (t instanceof WildcardType) {
				continue; // TODO: For now.
			}

			final Class<?> clazz = GenericTypeReflector.erase(t);
			if (!TestUsageChecker.canUse(GenericTypeReflector.erase(clazz))) {
				continue;
			}

			final GenericClass genericClass = new GenericClass(clazz).getWithWildcardTypes();
			if (!genericClass.satisfiesBoundaries(wildcardType, typeMap)) {
				logger.debug("Not assignable: " + clazz);
			} else {
				logger.debug("Assignable");
				assignableClasses.add(clazz);
			}
		}

		for (Type t : wildcardType.getUpperBounds()) {
			// Maps type variables to their instantiations
			if (t instanceof TypeVariable<?> && typeMap.containsKey(t)) {
				t = typeMap.get(t);
			}

			final Class<?> clazz = GenericTypeReflector.erase(t);
			logger.debug("Checking bound: " + t);

			if (!TestUsageChecker.canUse(clazz)) {
				continue;
			}

			final GenericClass genericClass = new GenericClass(t);
			if (genericClass.hasTypeVariables()) {
				logger.debug("Has type variables: " + genericClass);
				final GenericClass wildcardClass = genericClass.getWithWildcardTypes();
				if (!wildcardClass.satisfiesBoundaries(wildcardType, typeMap)) {
					logger.debug("Not assignable: " + clazz);
				} else {
					logger.debug("Assignable");
					assignableClasses.add(clazz);
				}
			} else {
				logger.debug("Adding directly: " + genericClass);
				assignableClasses.add(genericClass.getRawClass());
				classMap.put(genericClass, 10);
			}
		}

		logger.debug("Found assignable classes for wildcardtype " + wildcardType + ": "
		        + assignableClasses.size());

		if (!assignableClasses.isEmpty()) {
			final Class<?> clazz = Randomness.choice(assignableClasses);
			final GenericClass castClass = new GenericClass(clazz);
			logger.debug("Adding cast class " + castClass);
			classMap.put(castClass, 10);
			return true;
		}

		return false;
	}

	private boolean addAssignableClass(
			final TypeVariable<?> typeVariable,
	        final Map<TypeVariable<?>, Type> typeMap) {
		final Set<Class<?>> classes = TestCluster.getInstance().getAnalyzedClasses();
		final Set<Class<?>> assignableClasses = new LinkedHashSet<>();

		for (final Class<?> clazz : classes) {
			if (!TestUsageChecker.canUse(clazz)) {
				continue;
			}

			final GenericClass genericClass = new GenericClass(clazz).getWithWildcardTypes();

			if (!genericClass.satisfiesBoundaries(typeVariable, typeMap)) {
				logger.debug("Not assignable: " + clazz);
			} else {
				logger.debug("Assignable");
				assignableClasses.add(clazz);
			}
		}

		for (final Type t : typeMap.values()) {
			if (t instanceof WildcardType) {
				continue; // TODO: For now.
			}

			final Class<?> clazz = GenericTypeReflector.erase(t);
			if (!TestUsageChecker.canUse(GenericTypeReflector.erase(clazz))) {
				continue;
			}

			final GenericClass genericClass = new GenericClass(clazz).getWithWildcardTypes();
			if (!genericClass.satisfiesBoundaries(typeVariable, typeMap)) {
				logger.debug("Not assignable: " + clazz);
			} else {
				logger.debug("Assignable");
				assignableClasses.add(clazz);
			}
		}
		/*
		for (Type t : typeVariable.getBounds()) {
			if (typeMap.containsKey(t))
				t = typeMap.get(t);

			Class<?> clazz = GenericTypeReflector.erase(t);
			logger.debug("Checking bound: " + t);

			if (!TestClusterGenerator.canUse(clazz))
				continue;

			GenericClass genericClass = new GenericClass(t);
			//if (genericClass.hasTypeVariables()) {
			logger.debug("Has type variables: " + genericClass
			        + ", checking wildcard version with type map " + typeMap);
			GenericClass wildcardClass = genericClass.getWithWildcardTypes();
			//if (!wildcardClass.satisfiesBoundaries(typeVariable, typeMap)) {
			//	logger.debug("Not assignable: " + clazz);
			//} else {
			//	logger.debug("Assignable");
			assignableClasses.add(clazz);
			//}
			//} else {
			//	logger.debug("Adding directly: " + genericClass);
			//	assignableClasses.add(genericClass.getRawClass());
			//	classMap.put(genericClass, 10);
			//}
		}
		*/

		logger.debug("Found assignable classes for type variable " + typeVariable + ": "
		        + assignableClasses.size());

		if (!assignableClasses.isEmpty()) {
			final Class<?> clazz = Randomness.choice(assignableClasses);
			final GenericClass castClass = new GenericClass(clazz);
			logger.debug("Adding cast class " + castClass);
			classMap.put(castClass, 10);
			return true;
		}

		final InheritanceTree inheritanceTree = DependencyAnalysis.getInheritanceTree();
		final Set<Class<?>> boundCandidates = new LinkedHashSet<>();
		for (final Type bound : typeVariable.getBounds()) {
			final Class<?> rawBound = GenericTypeReflector.erase(bound);
			boundCandidates.add(rawBound);

			logger.debug("Getting concrete classes for " + rawBound);
			final Set<Class<?>> concreteClasses = ConcreteClassAnalyzer.getInstance().getConcreteClasses(rawBound,
					inheritanceTree);
			boundCandidates.addAll(concreteClasses);
		}

		for (final Class<?> clazz : boundCandidates) {
			if (!TestUsageChecker.canUse(clazz)) {
				continue;
			}

			boolean isAssignable = true;
			for (final Type bound : typeVariable.getBounds()) {
				if (GenericTypeReflector.erase(bound).equals(Enum.class) && clazz.isEnum()) {
					continue;
				}

				if (!GenericClass.isAssignable(bound, clazz)) {
					isAssignable = false;
					logger.debug("Not assignable: " + clazz + " to bound " + bound);
					break;
				}

				if (bound instanceof ParameterizedType) {
					final Type[] typeArgs = ((ParameterizedType) bound).getActualTypeArguments();
					if (Arrays.asList(typeArgs).contains(typeVariable)) {
						isAssignable = false;
						break;
					}
				}
			}

			if (isAssignable) {
				assignableClasses.add(clazz);
			}
		}

		logger.debug("After adding bounds, found " + assignableClasses.size() + " assignable classes for type variable "
				+ typeVariable + ": " + assignableClasses);

		if (!assignableClasses.isEmpty()) {
			// TODO: Add all classes?
//				for(Class<?> clazz : assignableClasses) {
//					GenericClass castClass = new GenericClass(clazz);
//					logger.debug("Adding cast class " + castClass);
//					classMap.put(castClass, 10);
//				}
			final Class<?> clazz = Randomness.choice(assignableClasses);
			final GenericClass castClass = new GenericClass(clazz);
			logger.debug("Adding cast class " + castClass);
			classMap.put(castClass, 10);
			return true;
		}

		return false;
	}

	private List<GenericClass> getAssignableClasses(
			final WildcardType wildcardType,
	        final boolean allowRecursion,
			final Map<TypeVariable<?>, Type> ownerVariableMap) {
		final Map<GenericClass, Integer> assignableClasses = new LinkedHashMap<>();

		logger.debug("Getting assignable classes for wildcard type " + wildcardType);
		for (final Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			final GenericClass clazz = entry.getKey();
			logger.debug("Current class for wildcard " + wildcardType + ": " + clazz);

			if (!clazz.satisfiesBoundaries(wildcardType, ownerVariableMap)) {
				logger.debug("Does not satisfy boundaries");
				continue;
			}

			if (!allowRecursion && clazz.hasWildcardOrTypeVariables()) {
				logger.debug("Stopping because of type recursion");
				continue;
			}
			logger.debug("Is assignable");

			final int priority = entry.getValue();
			assignableClasses.put(clazz, priority);
		}

		return sortByValue(assignableClasses);
	}

	private List<GenericClass> getAssignableClasses(
			final TypeVariable<?> typeVariable,
	        final boolean allowRecursion,
			final Map<TypeVariable<?>, Type> ownerVariableMap) {
		final Map<GenericClass, Integer> assignableClasses = new LinkedHashMap<>();

		logger.debug("Getting assignable classes for type variable " + typeVariable);
		for (final Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			final GenericClass clazz = entry.getKey();
			logger.debug("Current class for type variable " + typeVariable + ": " + clazz);

			if (!clazz.satisfiesBoundaries(typeVariable, ownerVariableMap)) {
				logger.debug("Bounds not satisfied");
				continue;
			}

			if (!allowRecursion && clazz.hasWildcardOrTypeVariables()) {
				logger.debug("Recursion not allowed but type has wildcard or type variables");
				continue;
			}

			assignableClasses.put(entry.getKey(), entry.getValue());
		}

		logger.debug("Found assignable classes: " + assignableClasses.size());

		return sortByValue(assignableClasses);
	}

	public GenericClass selectCastClass() {
		final List<GenericClass> assignableClasses = sortByValue(classMap);
		return selectClass(assignableClasses);
	}

	public GenericClass selectCastClass(
			final TypeVariable<?> typeVariable,
	        final boolean allowRecursion,
			final Map<TypeVariable<?>, Type> ownerVariableMap) {
		List<GenericClass> assignableClasses =
				getAssignableClasses(typeVariable, allowRecursion, ownerVariableMap);

		logger.debug("Assignable classes to " + typeVariable + ": " + assignableClasses);

		// FIXME: If we disallow recursion immediately, then we will never actually
		// do recursion since we always have Object, Integer, and String as candidates.
		// Including recursion may influence performance negatively.
		//
		// If we were not able to find an assignable class without recursive types
		// we try again but allowing recursion
//		if(assignableClasses.isEmpty()) {
//			assignableClasses = getAssignableClasses(typeVariable,
//                    allowRecursion,
//                    ownerVariableMap);
//		}

		// special case
		if (assignableClasses.isEmpty()) {
			logger.debug("Trying to add new cast class");
			if (addAssignableClass(typeVariable, ownerVariableMap)) {
				logger.debug("Now trying again to get a class");
				assignableClasses = getAssignableClasses(typeVariable, allowRecursion,
				                                         ownerVariableMap);
				if (assignableClasses.isEmpty()) {
					logger.debug("Nothing is assignable");
					return null;
				}
			} else {
				logger.debug("Nothing is assignable");
				return null;
			}
		}
		logger.debug("Now we've got assignable classes: " + assignableClasses.size());

		return selectClass(assignableClasses);
	}

	public GenericClass selectCastClass(
			final WildcardType wildcardType,
			final boolean allowRecursion, Map<TypeVariable<?>, Type> ownerVariableMap)
			throws ConstructionFailedException {
		logger.debug("Getting assignable classes for wildcard");
		List<GenericClass> assignableClasses =
				getAssignableClasses(wildcardType, false, ownerVariableMap);
		logger.debug("Assignable classes to " + wildcardType + ": " + assignableClasses);

		// If we were not able to find an assignable class without recursive types
		// we try again but allowing recursion
		if (assignableClasses.isEmpty() && allowRecursion) {
			assignableClasses.addAll(getAssignableClasses(wildcardType, allowRecursion, ownerVariableMap));
		}

		if (assignableClasses.isEmpty()) {
			logger.debug("Trying to add new cast class");

			if (addAssignableClass(wildcardType, ownerVariableMap)) {
				assignableClasses = getAssignableClasses(wildcardType, allowRecursion, ownerVariableMap);

				if (assignableClasses.isEmpty()) {
					logger.debug("Nothing is assignable");
					throw new ConstructionFailedException("Nothing is assignable to "
					        + wildcardType);
				}
			} else {
				logger.debug("Making random choice because nothing is assignable");
				throw new ConstructionFailedException("Nothing is assignable to "
				        + wildcardType);
			}
		}

		return selectClass(assignableClasses);
	}

	public boolean hasClass(final String className) {
		return classMap.keySet().stream()
				.anyMatch(clazz -> clazz.getClassName().equals(className));
	}

	public Set<GenericClass> getCastClasses() {
		return Collections.unmodifiableSet(classMap.keySet());
	}

	/**
	 * Clears all mappings
	 */
	public void clear() {
		classMap.clear();
		initDefaultClasses();
	}
}
