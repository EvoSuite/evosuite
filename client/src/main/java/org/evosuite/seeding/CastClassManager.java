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
package org.evosuite.seeding;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.ga.ConstructionFailedException;
import org.evosuite.setup.*;
import org.evosuite.utils.generic.GenericClass;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.gentyref.GenericTypeReflector;

public class CastClassManager {

	private static CastClassManager instance = new CastClassManager();

	private static final Logger logger = LoggerFactory.getLogger(CastClassManager.class);

	private final Map<GenericClass, Integer> classMap = new LinkedHashMap<GenericClass, Integer>();

	public static List<GenericClass> sortByValue(Map<GenericClass, Integer> map) {
		List<Map.Entry<GenericClass, Integer>> list = new LinkedList<Map.Entry<GenericClass, Integer>>(
		        map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<GenericClass, Integer>>() {
			@Override
			public int compare(Map.Entry<GenericClass, Integer> o1,
			        Map.Entry<GenericClass, Integer> o2) {
				if (o1.getKey().getNumParameters() == o2.getKey().getNumParameters())
					return (o1.getValue()).compareTo(o2.getValue());
				else
					return o1.getKey().getNumParameters()
					        - o2.getKey().getNumParameters();
			}
		});

		List<GenericClass> result = new LinkedList<GenericClass>();
		for (Map.Entry<GenericClass, Integer> entry : list) {
			result.add(entry.getKey());
		}
		return result;
	}

	public static GenericClass selectClass(List<GenericClass> candidates) {
		double r = Randomness.nextDouble();
		double d = Properties.RANK_BIAS
		        - Math.sqrt((Properties.RANK_BIAS * Properties.RANK_BIAS)
		                - (4.0 * (Properties.RANK_BIAS - 1.0) * r));
		int length = candidates.size();

		d = d / 2.0 / (Properties.RANK_BIAS - 1.0);

		int index = (int) (length * d);
		return candidates.get(index);
	}

	private CastClassManager() {
		initDefaultClasses();
	}

	private void initDefaultClasses() {
		classMap.put(new GenericClass(Object.class), 0);
		classMap.put(new GenericClass(String.class), 1);
		classMap.put(new GenericClass(Integer.class), 1);
	}

	public static CastClassManager getInstance() {
		return instance;
	}

	public void addCastClass(String className, int depth) {
		try {
			Class<?> clazz = TestGenerationContext.getInstance().getClassLoaderForSUT().loadClass(className);
			GenericClass castClazz = new GenericClass(clazz);
			addCastClass(castClazz.getWithWildcardTypes(), depth);
		} catch (ClassNotFoundException e) {
			// Ignore
			logger.debug("Error including cast class " + className + " because: " + e);
		}
	}

	public void addCastClass(Type type, int depth) {
		GenericClass castClazz = new GenericClass(type);
		addCastClass(castClazz.getWithWildcardTypes(), depth);
	}

	public void addCastClass(GenericClass clazz, int depth) {
		if (clazz.getRawClass() == null) {
			logger.warn("ADDING NULL!");
			assert (false);
		}
		if (clazz.isAbstract()) {
			for (Class<?> concreteClass : ConcreteClassAnalyzer.getInstance().getConcreteClasses(clazz.getRawClass(),
			                                                                      TestCluster.getInheritanceTree())) {
				GenericClass c = new GenericClass(concreteClass);
				if(TestUsageChecker.canUse(c.getRawClass())) {
					classMap.put(c, depth);
				}
			}

			if(Properties.P_FUNCTIONAL_MOCKING > 0.0) {
				if (TestUsageChecker.canUse(clazz.getRawClass()))
					classMap.put(clazz, depth);
			}

		}  else {
			if(TestUsageChecker.canUse(clazz.getRawClass()))
				classMap.put(clazz, depth);
		}
	}

	private void handleComparable() {
		// TODO
	}

	private void handleComparator() {
		// TODO
	}

	private void handleEnum() {
		// TODO
	}

	private void handleIterable() {
		// TODO
	}

	private final List<Class<?>> specialCases = Arrays.asList(new Class<?>[] {
	        Comparable.class, Comparator.class, Iterable.class, Enum.class });

	/**
	 * True if this type variable is one of the java.* special cases
	 *
	 * @return
	 */
	private boolean isSpecialCase(TypeVariable<?> typeVariable) {
		for (Type bound : typeVariable.getBounds()) {
			Class<?> clazz = GenericTypeReflector.erase(bound);
			if (specialCases.contains(clazz))
				return true;
		}
		return false;
	}

	/**
	 * True if this wildcard type is one of the java.* special cases
	 *
	 * @return
	 */
	private boolean isSpecialCase(WildcardType wildcardType) {
		// TODO
		return false;
	}

	private boolean addAssignableClass(WildcardType wildcardType,
	        Map<TypeVariable<?>, Type> typeMap) {
		Set<Class<?>> classes = TestCluster.getInstance().getAnalyzedClasses();
		Set<Class<?>> assignableClasses = new LinkedHashSet<Class<?>>();

		for (Class<?> clazz : classes) {
			if (!TestUsageChecker.canUse(clazz))
				continue;

			GenericClass genericClass = new GenericClass(clazz).getWithWildcardTypes();
			if (!genericClass.satisfiesBoundaries(wildcardType, typeMap)) {
				logger.debug("Not assignable: " + clazz);
			} else {
				logger.debug("Assignable");
				assignableClasses.add(clazz);
			}
		}
		for (Type t : typeMap.values()) {
			if (t instanceof WildcardType)
				continue; // TODO: For now.

			Class<?> clazz = GenericTypeReflector.erase(t);
			if (!TestUsageChecker.canUse(GenericTypeReflector.erase(clazz)))
				continue;

			GenericClass genericClass = new GenericClass(clazz).getWithWildcardTypes();
			if (!genericClass.satisfiesBoundaries(wildcardType, typeMap)) {
				logger.debug("Not assignable: " + clazz);
			} else {
				logger.debug("Assignable");
				assignableClasses.add(clazz);
			}
		}
		for (Type t : wildcardType.getUpperBounds()) {
			if (typeMap.containsKey(t))
				t = typeMap.get(t);

			Class<?> clazz = GenericTypeReflector.erase(t);
			logger.debug("Checking bound: " + t);

			if (!TestUsageChecker.canUse(clazz))
				continue;

			GenericClass genericClass = new GenericClass(t);
			if (genericClass.hasTypeVariables()) {
				logger.debug("Has type variables: " + genericClass);
				GenericClass wildcardClass = genericClass.getWithWildcardTypes();
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
			Class<?> clazz = Randomness.choice(assignableClasses);
			GenericClass castClass = new GenericClass(clazz);
			logger.debug("Adding cast class " + castClass);
			classMap.put(castClass, 10);
			return true;
		}

		return false;
	}

	private boolean addAssignableClass(TypeVariable<?> typeVariable,
	        Map<TypeVariable<?>, Type> typeMap) {
		Set<Class<?>> classes = TestCluster.getInstance().getAnalyzedClasses();
		Set<Class<?>> assignableClasses = new LinkedHashSet<Class<?>>();

		for (Class<?> clazz : classes) {
			if (!TestUsageChecker.canUse(clazz))
				continue;

			GenericClass genericClass = new GenericClass(clazz).getWithWildcardTypes();

			if (!genericClass.satisfiesBoundaries(typeVariable, typeMap)) {
				logger.debug("Not assignable: " + clazz);
			} else {
				logger.debug("Assignable");
				assignableClasses.add(clazz);
			}
		}
		for (Type t : typeMap.values()) {
			if (t instanceof WildcardType)
				continue; // TODO: For now.

			Class<?> clazz = GenericTypeReflector.erase(t);
			if (!TestUsageChecker.canUse(GenericTypeReflector.erase(clazz)))
				continue;

			GenericClass genericClass = new GenericClass(clazz).getWithWildcardTypes();
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
			Class<?> clazz = Randomness.choice(assignableClasses);
			GenericClass castClass = new GenericClass(clazz);
			logger.debug("Adding cast class " + castClass);
			classMap.put(castClass, 10);
			return true;
		} else {
			InheritanceTree inheritanceTree = DependencyAnalysis.getInheritanceTree();
			Set<Class<?>> boundCandidates = new LinkedHashSet<Class<?>>();
			for (Type bound : typeVariable.getBounds()) {
				Class<?> rawBound = GenericTypeReflector.erase(bound);
				boundCandidates.add(rawBound);
				logger.debug("Getting concrete classes for " + rawBound);
				boundCandidates.addAll(ConcreteClassAnalyzer.getInstance().getConcreteClasses(rawBound,
				                                                               inheritanceTree));
			}
			for (Class<?> clazz : boundCandidates) {
				if (!TestUsageChecker.canUse(clazz))
					continue;

				boolean isAssignable = true;
				for (Type bound : typeVariable.getBounds()) {
					if (GenericTypeReflector.erase(bound).equals(Enum.class)) {
						if (clazz.isEnum())
							continue;
					}

					if (!GenericClass.isAssignable(bound, clazz)) {
						isAssignable = false;
						logger.debug("Not assignable: " + clazz + " to bound " + bound);
						break;
					}
					if (bound instanceof ParameterizedType) {
						if (Arrays.asList(((ParameterizedType) bound).getActualTypeArguments()).contains(typeVariable)) {
							isAssignable = false;
							break;
						}
					}
				}
				if (isAssignable) {
					assignableClasses.add(clazz);
				}
			}
			logger.debug("After adding bounds, found "+assignableClasses.size()+" assignable classes for type variable "
			        + typeVariable + ": " + assignableClasses);
			if (!assignableClasses.isEmpty()) {
				// TODO: Add all classes?
//				for(Class<?> clazz : assignableClasses) {
//					GenericClass castClass = new GenericClass(clazz);
//					logger.debug("Adding cast class " + castClass);
//					classMap.put(castClass, 10);
//				}
				Class<?> clazz = Randomness.choice(assignableClasses);
				GenericClass castClass = new GenericClass(clazz);
				logger.debug("Adding cast class " + castClass);
				classMap.put(castClass, 10);
				return true;
			}

		}

		return false;
	}

	private List<GenericClass> getAssignableClasses(WildcardType wildcardType,
	        boolean allowRecursion, Map<TypeVariable<?>, Type> ownerVariableMap) {
		Map<GenericClass, Integer> assignableClasses = new LinkedHashMap<GenericClass, Integer>();

		logger.debug("Getting assignable classes for wildcard type " + wildcardType);
		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			GenericClass key = entry.getKey();
			logger.debug("Current class for wildcard " + wildcardType + ": " + key);

			if (!key.satisfiesBoundaries(wildcardType, ownerVariableMap)) {
				logger.debug("Does not satisfy boundaries");
				continue;
			}

			if (!allowRecursion && key.hasWildcardOrTypeVariables()) {
				logger.debug("Stopping because of type recursion");
				continue;
			}
			logger.debug("Is assignable");

			assignableClasses.put(entry.getKey(), entry.getValue());
		}

		return sortByValue(assignableClasses);
	}

	private List<GenericClass> getAssignableClasses(TypeVariable<?> typeVariable,
	        boolean allowRecursion, Map<TypeVariable<?>, Type> ownerVariableMap) {
		Map<GenericClass, Integer> assignableClasses = new LinkedHashMap<GenericClass, Integer>();

		logger.debug("Getting assignable classes for type variable " + typeVariable);
		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			GenericClass key = entry.getKey();
			logger.debug("Current class for type variable " + typeVariable + ": " + key);

			if (!key.satisfiesBoundaries(typeVariable, ownerVariableMap)) {
				logger.debug("Bounds not satisfied");
				continue;
			}
			if (!allowRecursion && key.hasWildcardOrTypeVariables()) {
				logger.debug("Recursion not allowed but type has wilcard or type variables");
				continue;
			}

			assignableClasses.put(entry.getKey(), entry.getValue());
		}
		logger.debug("Found assignable classes: " + assignableClasses.size());

		return sortByValue(assignableClasses);
	}

	public GenericClass selectCastClass() {

		List<GenericClass> assignableClasses = sortByValue(classMap);
		return selectClass(assignableClasses);
	}

	public GenericClass selectCastClass(TypeVariable<?> typeVariable,
	        boolean allowRecursion, Map<TypeVariable<?>, Type> ownerVariableMap) {

		List<GenericClass> assignableClasses = getAssignableClasses(typeVariable,
		                                                            false,
		                                                            ownerVariableMap);

		logger.debug("Assignable classes to " + typeVariable + ": " + assignableClasses);

		// If we were not able to find an assignable class without recursive types
		// we try again but allowing recursion
		if(assignableClasses.isEmpty()) {
			assignableClasses = getAssignableClasses(typeVariable,
                    allowRecursion,
                    ownerVariableMap);
		}

		//special case
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

	public GenericClass selectCastClass(WildcardType wildcardType,
	        boolean allowRecursion, Map<TypeVariable<?>, Type> ownerVariableMap)
	        throws ConstructionFailedException {

		logger.debug("Getting assignable classes for wildcard");
		List<GenericClass> assignableClasses = getAssignableClasses(wildcardType,
		                                                            false,
		                                                            ownerVariableMap);
		logger.debug("Assignable classes to " + wildcardType + ": " + assignableClasses);

		// If we were not able to find an assignable class without recursive types
		// we try again but allowing recursion
		if (assignableClasses.isEmpty()) {
			if(allowRecursion) {
			assignableClasses.addAll(getAssignableClasses(wildcardType,
                    allowRecursion,
                    ownerVariableMap));
			}
		}

		if (assignableClasses.isEmpty()) {

			logger.debug("Trying to add new cast class");
			if (addAssignableClass(wildcardType, ownerVariableMap)) {
				assignableClasses = getAssignableClasses(wildcardType, allowRecursion,
				                                         ownerVariableMap);
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

	public boolean hasClass(String className) {
		for (GenericClass clazz : classMap.keySet()) {
			if (clazz.getClassName().equals(className))
				return true;
		}
		return false;
	}

	public Set<GenericClass> getCastClasses() {
		return classMap.keySet();
	}

	public void clear() {
		classMap.clear();
		initDefaultClasses();
	}

}
