/**
 * 
 */
package de.unisb.cs.st.evosuite.coverage.path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gordon Fraser
 * 
 */
public class PrimePathPool {

	// maps: className -> methodName  -> DUVarName -> branchID -> List of Definitions in that branch 
	public static Map<String, Map<String, List<PrimePath>>> primePathMap = new HashMap<String, Map<String, List<PrimePath>>>();

	public static int primePathCounter = 0;

	public static int getSize() {
		return primePathCounter;
	}

	public static void add(PrimePath path) {
		String className = path.className;
		String methodName = path.methodName;

		if (!primePathMap.containsKey(className))
			primePathMap.put(className, new HashMap<String, List<PrimePath>>());
		if (!primePathMap.get(className).containsKey(methodName))
			primePathMap.get(className).put(methodName, new ArrayList<PrimePath>());
		path.condensate();
		primePathMap.get(className).get(methodName).add(path);
		primePathCounter++;
	}
}
