package org.evosuite.setup;

import java.lang.reflect.Type;
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
			classMap.put(new GenericClass(Object.class), 2);
	}
	
	public static CastClassManager getInstance() {
		return instance;
	}
	
	private void sortClassMap() {
		List<Map.Entry<GenericClass, Integer>> entries =
				new ArrayList<Map.Entry<GenericClass, Integer>>(classMap.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<GenericClass, Integer>>() {
			public int compare(Map.Entry<GenericClass, Integer> a, Map.Entry<GenericClass, Integer> b){
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
			Class<?> clazz = TestGenerationContext.getClassLoader().loadClass(
					className);
			GenericClass castClazz = new GenericClass(clazz);
			addCastClass(castClazz, depth);
		} catch (ClassNotFoundException e) {
			// Ignore
			logger.debug("Error including cast class " + className
					+ " because: " + e);
		}
	}
	
	public void addCastClass(Type type, int depth) {
		GenericClass castClazz = new GenericClass(type);
		addCastClass(castClazz, depth);
	}
	
	public void addCastClass(GenericClass clazz, int depth) {
		if(classMap.containsKey(clazz))
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
			double v = depth == 0 ? 0.0 : 1.0/depth;
			sumValue += v;
		}
		changed = false;
	}
	
	private int getSum(Type type, boolean allowRecursion) {
		int sum = 0;
		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			if(!entry.getKey().isAssignableTo(type))
				continue;
			
			if(!allowRecursion && entry.getKey().hasWildcardOrTypeVariables())
					continue;
			
			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0/depth;
			sum += v;
		}
		return sum;
	}

	
	public GenericClass selectCastClass() {
		
		if(changed)
			setSum();
		
		//special case
		if (sumValue == 0d) {
			return Randomness.choice(classMap.keySet());
		}

		double rnd = Randomness.nextDouble() * sumValue;
		
		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0/depth;

			if(v >= rnd)
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
		
		int sum = getSum(targetType, allowRecursion);
		
		//special case
		if (sum == 0d) {
			return Randomness.choice(classMap.keySet());
		}

		double rnd = Randomness.nextDouble() * sum;
		
		for (Entry<GenericClass, Integer> entry : classMap.entrySet()) {
			if(!entry.getKey().isAssignableTo(targetType))
				continue;
			
			if(!allowRecursion && entry.getKey().hasWildcardOrTypeVariables())
				continue;

			int depth = entry.getValue();
			double v = depth == 0 ? 0.0 : 1.0/depth;

			if(v >= rnd)
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
		for(GenericClass clazz : classMap.keySet()) {
			if(clazz.getClassName().equals(className))
				return true;
		}
		return false;
	}
	
	public Set<GenericClass> getCastClasses() {
		return classMap.keySet();
	}
	
	public void clear() {
		classMap.clear();
		classMap.put(new GenericClass(Object.class), 2);
		changed = true;
	}

}
