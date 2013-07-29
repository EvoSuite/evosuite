package org.evosuite.setup;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.evosuite.TestGenerationContext;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.gentyref.GenericTypeReflector;

public class CastClassManager {

	private static CastClassManager instance = new CastClassManager();

	private static final Logger logger = LoggerFactory.getLogger(CastClassManager.class);

	private Map<GenericClass, Integer> classMap = new LinkedHashMap<GenericClass, Integer>();

	private boolean changed = false;

	private double sumValue = 0d;

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

	private void sortClassMap() {
		List<Map.Entry<GenericClass, Integer>> entries = new ArrayList<Map.Entry<GenericClass, Integer>>(
		        classMap.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<GenericClass, Integer>>() {
			@Override
			public int compare(Map.Entry<GenericClass, Integer> a,
			        Map.Entry<GenericClass, Integer> b) {
				return a.getValue().compareTo(b.getValue());
			}
		});
		Map<GenericClass, Integer> sortedMap = new LinkedHashMap<GenericClass, Integer>();
		for (Map.Entry<GenericClass, Integer> entry : entries) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		classMap = sortedMap;
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
		if(clazz.getRawClass() == null) {
			logger.warn("ADDING NULL!");
			assert(false);
		}
		if(clazz.isAbstract()) {
			for(Class<?> concreteClass : TestClusterGenerator.getConcreteClasses(clazz.getRawClass(), TestCluster.getInheritanceTree())) {
				GenericClass c = new GenericClass(concreteClass);
				classMap.put(c, depth);					
			}
		} else {
			classMap.put(clazz, depth);
		}
		sortClassMap();
		changed = true;
	}

	/**
	 * Calculate total sum of fitnesses
	 * 
	 * @param population
	 */
	private void setSum() {
		sumValue = 0;
		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0 / depth;
			sumValue += v;
		}
		changed = false;
	}

	private double getSum(Type type, boolean allowRecursion) {
		double sum = 0d;
		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			if (!entry.getKey().isAssignableTo(type)) {
				logger.debug("Not assignable: "+entry.getKey()+" to "+type);
				if(type instanceof TypeVariable<?>) {
					logger.debug("Bounds: "+Arrays.asList(((TypeVariable<?>)type).getBounds()));
				}
				continue;
			}

			if (!allowRecursion && entry.getKey().hasWildcardOrTypeVariables()) {
				logger.debug("Has wildcard, but not recursion possible: "+entry.getKey()+" to "+type);
				continue;
			}

			logger.debug("Is assignable: "+entry.getKey()+" to "+type);

			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0 / depth;
			sum += v;
		}
		return sum;
	}

	private boolean addAssignableClass(WildcardType wildcardTpe) {
		// TODO
		return true;
	}
	
	private boolean addAssignableClass(TypeVariable<?> typeVariable) {
		Set<Class<?>> classes = TestCluster.getInstance().getAnalyzedClasses();
		Set<Class<?>> assignableClasses = new LinkedHashSet<Class<?>>();

		for (Class<?> clazz : classes) {
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
					// TODO:
					// if (GenericTypeReflector.erase(bound).isAssignableFrom(clazz)) {

					// }

					break;
				}
			}
			if (isAssignable) {
				assignableClasses.add(clazz);
			}
		}
		logger.debug("Found assignable classes for type variable " + typeVariable + ": "
		        + assignableClasses.size());
		if (!assignableClasses.isEmpty()) {
			Class<?> clazz = Randomness.choice(assignableClasses);
			GenericClass castClass = new GenericClass(clazz);
			logger.debug("Adding cast class " + castClass);
			classMap.put(castClass, 10);
			sortClassMap();
			return true;
		} else {
			InheritanceTree inheritanceTree = DependencyAnalysis.getInheritanceTree();
			Set<Class<?>> boundCandidates = new LinkedHashSet<Class<?>>();
			for (Type bound : typeVariable.getBounds()) {
				Class<?> rawBound = GenericTypeReflector.erase(bound);
				boundCandidates.add(rawBound);
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
				sortClassMap();
				return true;
			}

		}

		return false;
	}
	
	private double getSum(WildcardType wildcardType, boolean allowRecursion,
			Map<TypeVariable<?>, Type> ownerVariableMap) {

		double sum = 0.0;

		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			logger.debug("Entry " + entry.getKey().getTypeName() + " at depth "
					+ entry.getValue());

			GenericClass key = entry.getKey();

			if (!key.satisfiesBoundaries(wildcardType)) {
				continue;
			}

			logger.debug("assignable: " + key.getTypeName());

			if (!allowRecursion && key.hasWildcardOrTypeVariables()) {
				continue;
			}

			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0 / depth;
			sum += v;
		}
		return sum;
	}

	private double getSum(TypeVariable<?> typeVariable, boolean allowRecursion,
	        Map<TypeVariable<?>, Type> ownerVariableMap) {
		double sum = 0d;

		/**
		 * Maybe need to do this recursively: - Try to find class that is
		 * assignable to all bounds - If it is assignable, then the chosen cast
		 * class is a generic subclass of the bound -
		 * 
		 */

		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			logger.debug("Entry " + entry.getKey().getTypeName() + " at depth "
			        + entry.getValue());
			
			logger.debug("Getting instance for type variable with bounds "
			        + Arrays.asList(TypeUtils.getImplicitBounds(typeVariable))
			        + " and map " + ownerVariableMap);
			GenericClass key = entry.getKey();
			
			if (!key.satisfiesBoundaries(typeVariable)) {
				continue;
			}

			logger.debug("assignable: " + key.getTypeName());

			if (!allowRecursion && key.hasWildcardOrTypeVariables()) {
				continue;
			}

			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0 / depth;
			sum += v;
		}
		return sum;
	}

	public GenericClass selectCastClass() {

		if (changed)
			setSum();

		//special case
		if (sumValue == 0d) {
			return Randomness.choice(classMap.keySet());
		}

		double rnd = Randomness.nextDouble() * sumValue;

		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0 / depth;

			if (v >= rnd)
				return entry.getKey();
			else
				rnd = rnd - v;
		}

		//now this should never happens, but possible issues with rounding errors in for example "rnd = rnd - fit"
		//in such a case, we just return a random index and we log it

		logger.debug("ATTENTION: Possible issue in CastClassManager");
		return Randomness.choice(classMap.keySet());
	}

	public GenericClass selectCastClass(Type targetType, boolean allowRecursion) {

		// TODO: Need to check bounds on wildcard types!
		double sum = getSum(targetType, allowRecursion);

		//special case
		if (sum == 0d) {
			logger.warn("Found no assignable classes for type "+targetType);
			assert(false);
			return Randomness.choice(classMap.keySet());
		}

		double rnd = Randomness.nextDouble() * sum;
		logger.debug("Getting cast class for type " + targetType);

		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			logger.debug("Candidate cast class: " + entry.getKey());
			if (targetType instanceof WildcardType) {
				WildcardType wc = (WildcardType) targetType;
				logger.debug("Bounds of wildcardtype: "
				        + Arrays.asList(wc.getLowerBounds()) + " / "
				        + Arrays.asList(wc.getUpperBounds()));
			}
			if (!entry.getKey().isAssignableTo(targetType)) {
				logger.debug("Is not assignable to " + targetType);
				continue;
			}

			if (!allowRecursion && entry.getKey().hasWildcardOrTypeVariables()) {
				logger.debug("Would lead to forbidden type recursion");
				continue;
			}
			logger.debug("Is assignable to " + targetType);

			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0 / depth;

			if (v >= rnd)
				return entry.getKey();
			else
				rnd = rnd - v;
		}

		//now this should never happens, but possible issues with rounding errors in for example "rnd = rnd - fit"
		//in such a case, we just return a random index and we log it

		logger.debug("ATTENTION: Possible issue in CastClassManager");
		assert(false);
		return Randomness.choice(classMap.keySet());
	}

	public GenericClass selectCastClass(TypeVariable<?> typeVariable,
	        boolean allowRecursion, Map<TypeVariable<?>, Type> ownerVariableMap) {
		double sum = getSum(typeVariable, allowRecursion, ownerVariableMap);

		//special case
		if (sum == 0d) {
			logger.debug("Trying to add new cast class");
			if (addAssignableClass(typeVariable)) {
				return selectCastClass(typeVariable, allowRecursion, ownerVariableMap);
			}

			logger.debug("Making random choice because nothing is assignable");
			assert(false);
			return Randomness.choice(classMap.keySet());
		}

		double rnd = Randomness.nextDouble() * sum;
		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			GenericClass key = entry.getKey();
			
			if (!key.satisfiesBoundaries(typeVariable)) {
				continue;
			}
			logger.debug("Is assignable: " + entry.getKey() + " to " + typeVariable);

			if (!allowRecursion && key.hasWildcardOrTypeVariables())
				continue;

			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0 / depth;

			if (v >= rnd) {
				assert(key.getRawClass() != null);
				return key;
			}
			else
				rnd = rnd - v;
		}

		//now this should never happens, but possible issues with rounding errors in for example "rnd = rnd - fit"
		//in such a case, we just return a random index and we log it

		logger.warn("ATTENTION: Possible issue in CastClassManager");
		assert(false);
		return Randomness.choice(classMap.keySet());
	}
	
	
	public GenericClass selectCastClass(WildcardType wildcardType,
	        boolean allowRecursion, Map<TypeVariable<?>, Type> ownerVariableMap) {
		double sum = getSum(wildcardType, allowRecursion, ownerVariableMap);

		//special case
		if (sum == 0d) {
			logger.debug("Trying to add new cast class");
			if (addAssignableClass(wildcardType)) {
				return selectCastClass(wildcardType, allowRecursion, ownerVariableMap);
			}

			logger.debug("Making random choice because nothing is assignable");
			assert(false);
			return Randomness.choice(classMap.keySet());
		}

		double rnd = Randomness.nextDouble() * sum;
		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			GenericClass key = entry.getKey();
			if (!key.satisfiesBoundaries(wildcardType)) {
				continue;
			}
			logger.debug("Is assignable: " + entry.getKey() + " to " + wildcardType);

			if (!allowRecursion && key.hasWildcardOrTypeVariables())
				continue;

			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0 / depth;

			if (v >= rnd) {
				assert(key.getRawClass() != null);
				return key;
			}
			else
				rnd = rnd - v;
		}

		//now this should never happens, but possible issues with rounding errors in for example "rnd = rnd - fit"
		//in such a case, we just return a random index and we log it

		logger.warn("ATTENTION: Possible issue in CastClassManager");
		assert(false);
		return Randomness.choice(classMap.keySet());
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
		changed = true;
	}

}
