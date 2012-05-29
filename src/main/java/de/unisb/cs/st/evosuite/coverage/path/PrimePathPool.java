/**
 * Copyright (C) 2012 Gordon Fraser, Andrea Arcuri
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
