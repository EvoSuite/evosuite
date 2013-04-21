package org.evosuite.setup;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.evosuite.TestGenerationContext;
import org.evosuite.utils.GenericClass;
import org.evosuite.utils.GenericUtils;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
			addCastClass(castClazz, depth);
		} catch (ClassNotFoundException e) {
			// Ignore
			logger.debug("Error including cast class " + className + " because: " + e);
		}
	}

	public void addCastClass(Type type, int depth) {
		GenericClass castClazz = new GenericClass(type);
		addCastClass(castClazz, depth);
	}

	public void addCastClass(GenericClass clazz, int depth) {
		if (classMap.containsKey(clazz))
			return;

		classMap.put(clazz, depth);
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
				continue;
			}

			if (!allowRecursion && entry.getKey().hasWildcardOrTypeVariables()) {
				continue;
			}

			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0 / depth;
			sum += v;
		}
		return sum;
	}
	
	private double getSum(TypeVariable<?> typeVariable, boolean allowRecursion) {
		double sum = 0d;
		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			boolean isAssignable = true;
			for (Type theType : typeVariable.getBounds()) {
				Type type = GenericUtils.replaceTypeVariable(theType, typeVariable, entry.getKey().getType());
				if (!entry.getKey().isAssignableTo(type)) {// && !entry.getKey().isGenericSuperTypeOf(type)) {
					isAssignable = false;
					break;
				}
			}
			if (!isAssignable) {
				continue;
			}

			if (!allowRecursion && entry.getKey().hasWildcardOrTypeVariables()) {
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

		double sum = getSum(targetType, allowRecursion);

		//special case
		if (sum == 0d) {
			return Randomness.choice(classMap.keySet());
		}

		double rnd = Randomness.nextDouble() * sum;
		logger.debug("Getting cast class for type "+targetType);

		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			logger.debug("Candidate cast class: "+entry.getKey());
			if (!entry.getKey().isAssignableTo(targetType)) {
				logger.debug("Is not assignable to "+targetType);
				continue;
			}

			if (!allowRecursion && entry.getKey().hasWildcardOrTypeVariables()) {
				logger.debug("Would lead to forbidden type recursion");
				continue;
			}
			logger.debug("Is assignable to "+targetType);

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

	public GenericClass selectCastClass(TypeVariable<?> typeVariable,
	        boolean allowRecursion) {
		double sum = getSum(typeVariable, allowRecursion);

		//special case
		if (sum == 0d) {
			logger.debug("Making random choice because nothing is assignable");
			return Randomness.choice(classMap.keySet());
		}

		double rnd = Randomness.nextDouble() * sum;
		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			boolean isAssignable = true;
			for (Type theType : typeVariable.getBounds()) {
				Type type = GenericUtils.replaceTypeVariable(theType, typeVariable, entry.getKey().getType());
				if (!entry.getKey().isAssignableTo(type)) {// && !entry.getKey().isGenericSuperTypeOf(type)) {
					isAssignable = false;
					break;
				}
			}
			if (!isAssignable) {
				continue;
			}

			if (!allowRecursion && entry.getKey().hasWildcardOrTypeVariables())
				continue;

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
