/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
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
package de.unisb.cs.st.evosuite.coverage.mutation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

/**
 * @author fraser
 * 
 */
public class MutationPool {

	// maps className -> method inside that class -> list of branches inside that method 
	private static Map<String, Map<String, List<Mutation>>> mutationMap = new HashMap<String, Map<String, List<Mutation>>>();

	// maps the mutationIDs assigned by this pool to their respective Mutations
	private static Map<Integer, Mutation> mutationIdMap = new HashMap<Integer, Mutation>();

	private static int numMutations = 0;

	public static Mutation addMutation(String className, String methodName,
	        String mutationName, BytecodeInstruction instruction,
	        AbstractInsnNode mutation, InsnList distance) {

		if (!mutationMap.containsKey(className))
			mutationMap.put(className, new HashMap<String, List<Mutation>>());

		if (!mutationMap.get(className).containsKey(methodName))
			mutationMap.get(className).put(methodName, new ArrayList<Mutation>());

		Mutation mutationObject = new Mutation(className, methodName, mutationName,
		        numMutations++, instruction, mutation, distance);
		mutationMap.get(className).get(methodName).add(mutationObject);
		mutationIdMap.put(mutationObject.getId(), mutationObject);

		return mutationObject;
	}

	public static Mutation addMutation(String className, String methodName,
	        String mutationName, BytecodeInstruction instruction, InsnList mutation,
	        InsnList distance) {

		if (!mutationMap.containsKey(className))
			mutationMap.put(className, new HashMap<String, List<Mutation>>());

		if (!mutationMap.get(className).containsKey(methodName))
			mutationMap.get(className).put(methodName, new ArrayList<Mutation>());

		Mutation mutationObject = new Mutation(className, methodName, mutationName,
		        numMutations++, instruction, mutation, distance);
		mutationMap.get(className).get(methodName).add(mutationObject);
		mutationIdMap.put(mutationObject.getId(), mutationObject);

		return mutationObject;
	}

	/**
	 * Returns a List containing all mutants in the given class and method
	 * 
	 * Should no such mutant exist an empty List is returned
	 */
	public static List<Mutation> retrieveMutationsInMethod(String className,
	        String methodName) {
		List<Mutation> r = new ArrayList<Mutation>();
		if (mutationMap.get(className) == null)
			return r;
		List<Mutation> mutants = mutationMap.get(className).get(methodName);
		if (mutants != null)
			r.addAll(mutants);
		return r;
	}

	public static List<Mutation> getMutants() {
		List<Mutation> mutants = new ArrayList<Mutation>();
		mutants.addAll(mutationIdMap.values());
		return mutants;
	}

	/**
	 * Remove all known mutants
	 */
	public static void clear() {
		mutationMap.clear();
		mutationIdMap.clear();
		numMutations = 0;
	}

	/**
	 * Returns the number of currently known mutants
	 * 
	 * @return The number of currently known mutants
	 */
	public static int getMutantCounter() {
		return numMutations;
	}
}
