package org.evosuite.setup;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.TestGenerationContext;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.gentyref.GenericTypeReflector;

public class CastClassManager {

	private static CastClassManager instance = new CastClassManager();

	private static final Logger logger = LoggerFactory.getLogger(CastClassManager.class);

	private final Map<GenericClass, Integer> classMap = new LinkedHashMap<GenericClass, Integer>();

	public static <K, V extends Comparable<? super V>> List<K> sortByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		List<K> result = new LinkedList<K>();
		for (Map.Entry<K, V> entry : list) {
			result.add(entry.getKey());
		}
		return result;
	}

	private GenericClass selectClass(List<GenericClass> candidates) {
		double r = Randomness.nextDouble();
		double d = Properties.RANK_BIAS
		        - Math.sqrt((Properties.RANK_BIAS * Properties.RANK_BIAS)
		                - (4.0 * (Properties.RANK_BIAS - 1.0) * r));
		int length = candidates.size();

		d = d / 2.0 / (Properties.RANK_BIAS - 1.0);

		//this is not needed because population is sorted based on Maximization
		//if(maximize)
		d = 1.0 - d; // to do that if we want to have Maximisation

		int index = (int) (length * d);
		return candidates.get(index);
	}

	private CastClassManager() {
		initDefaultClasses();
	}

	private void initDefaultClasses() {
		classMap.put(new GenericClass(Object.class), 2);
		classMap.put(new GenericClass(String.class), 2);
		classMap.put(new GenericClass(Integer.class), 2);
	}

	public static CastClassManager getInstance() {
		return instance;
	}

	public void addCastClass(String className, int depth) {
		try {
			Class<?> clazz = TestGenerationContext.getClassLoader().loadClass(className);
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
			for (Class<?> concreteClass : TestClusterGenerator.getConcreteClasses(clazz.getRawClass(),
			                                                                      TestCluster.getInheritanceTree())) {
				GenericClass c = new GenericClass(concreteClass);
				classMap.put(c, depth);
			}
		} else {
			classMap.put(clazz, depth);
		}
	}

	private List<GenericClass> getAssignableClasses(Type type, boolean allowRecursion) {
		Map<GenericClass, Integer> assignableClasses = new HashMap<GenericClass, Integer>();

		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			if (!entry.getKey().isAssignableTo(type)) {
				continue;
			}

			if (!allowRecursion && entry.getKey().hasWildcardOrTypeVariables()) {
				logger.debug("Has wildcard, but not recursion possible: "
				        + entry.getKey() + " to " + type);
				continue;
			}

			assignableClasses.put(entry.getKey(), entry.getValue());
		}

		return sortByValue(assignableClasses);
	}

	private boolean addAssignableClass(WildcardType wildcardType,
	        Map<TypeVariable<?>, Type> typeMap) {
		Set<Class<?>> classes = TestCluster.getInstance().getAnalyzedClasses();
		Set<Class<?>> assignableClasses = new LinkedHashSet<Class<?>>();

		for (Class<?> clazz : classes) {
			if (!TestClusterGenerator.canUse(clazz))
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
			if (!TestClusterGenerator.canUse(GenericTypeReflector.erase(clazz)))
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

			if (!TestClusterGenerator.canUse(clazz))
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
			if (!TestClusterGenerator.canUse(clazz))
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
			if (!TestClusterGenerator.canUse(GenericTypeReflector.erase(clazz)))
				continue;

			GenericClass genericClass = new GenericClass(clazz).getWithWildcardTypes();
			if (!genericClass.satisfiesBoundaries(typeVariable, typeMap)) {
				logger.debug("Not assignable: " + clazz);
			} else {
				logger.debug("Assignable");
				assignableClasses.add(clazz);
			}
		}
		for (Type t : typeVariable.getBounds()) {
			if (typeMap.containsKey(t))
				t = typeMap.get(t);

			Class<?> clazz = GenericTypeReflector.erase(t);
			logger.debug("Checking bound: " + t);

			if (!TestClusterGenerator.canUse(clazz))
				continue;

			GenericClass genericClass = new GenericClass(t);
			if (genericClass.hasTypeVariables()) {
				logger.debug("Has type variables: " + genericClass);
				GenericClass wildcardClass = genericClass.getWithWildcardTypes();
				if (!wildcardClass.satisfiesBoundaries(typeVariable, typeMap)) {
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
				boundCandidates.addAll(TestClusterGenerator.getConcreteClasses(rawBound,
				                                                               inheritanceTree));
			}
			for (Class<?> clazz : boundCandidates) {
				if (!TestClusterGenerator.canUse(clazz))
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
			logger.debug("After adding bounds, found assignable classes for type variable "
			        + typeVariable + ": " + assignableClasses.size());
			if (!assignableClasses.isEmpty()) {
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
		Map<GenericClass, Integer> assignableClasses = new HashMap<GenericClass, Integer>();

		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			GenericClass key = entry.getKey();

			if (!key.satisfiesBoundaries(wildcardType, ownerVariableMap)) {
				continue;
			}

			if (!allowRecursion && key.hasWildcardOrTypeVariables()) {
				continue;
			}

			assignableClasses.put(entry.getKey(), entry.getValue());
		}

		return sortByValue(assignableClasses);
	}

	private List<GenericClass> getAssignableClasses(TypeVariable<?> typeVariable,
	        boolean allowRecursion, Map<TypeVariable<?>, Type> ownerVariableMap) {
		Map<GenericClass, Integer> assignableClasses = new HashMap<GenericClass, Integer>();

		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			GenericClass key = entry.getKey();

			if (!key.satisfiesBoundaries(typeVariable, ownerVariableMap)) {
				continue;
			}
			if (!allowRecursion && key.hasWildcardOrTypeVariables()) {
				continue;
			}

			assignableClasses.put(entry.getKey(), entry.getValue());
		}

		return sortByValue(assignableClasses);
	}

	public GenericClass selectCastClass() {

		List<GenericClass> assignableClasses = sortByValue(classMap);
		return selectClass(assignableClasses);
	}

	public GenericClass selectCastClass(Type targetType, boolean allowRecursion) {

		List<GenericClass> candidateClasses = getAssignableClasses(targetType,
		                                                           allowRecursion);
		logger.debug("Assignable classes to " + targetType + ": " + candidateClasses);
		if (candidateClasses.isEmpty()) {
			logger.warn("Found no assignable classes for type " + targetType);
			assert (false);
			return Randomness.choice(classMap.keySet());
		}

		return selectClass(candidateClasses);
	}

	public GenericClass selectCastClass(TypeVariable<?> typeVariable,
	        boolean allowRecursion, Map<TypeVariable<?>, Type> ownerVariableMap) {

		List<GenericClass> assignableClasses = getAssignableClasses(typeVariable,
		                                                            allowRecursion,
		                                                            ownerVariableMap);

		logger.debug("Assignable classes to " + typeVariable + ": " + assignableClasses);

		//special case
		if (assignableClasses.isEmpty()) {
			logger.debug("Trying to add new cast class");
			if (addAssignableClass(typeVariable, ownerVariableMap)) {
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

		return selectClass(assignableClasses);
	}

	public GenericClass selectCastClass(WildcardType wildcardType,
	        boolean allowRecursion, Map<TypeVariable<?>, Type> ownerVariableMap) {

		List<GenericClass> assignableClasses = getAssignableClasses(wildcardType,
		                                                            allowRecursion,
		                                                            ownerVariableMap);
		logger.debug("Assignable classes to " + wildcardType + ": " + assignableClasses);

		if (assignableClasses.isEmpty()) {
			logger.debug("Trying to add new cast class");
			if (addAssignableClass(wildcardType, ownerVariableMap)) {
				assignableClasses = getAssignableClasses(wildcardType, allowRecursion,
				                                         ownerVariableMap);
				if (assignableClasses.isEmpty()) {

					logger.debug("Nothing is assignable");
					return null;
				}
			} else {

				logger.debug("Making random choice because nothing is assignable");
				return null;

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
