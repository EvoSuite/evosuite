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

package org.evosuite.coverage.mutation;

import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;

import java.util.*;


/**
 * <p>MutationPool class.</p>
 *
 * @author fraser
 */
public class MutationPool {
    private static final Map<ClassLoader, MutationPool> instanceMap = new HashMap<>();

    public static MutationPool getInstance(ClassLoader classLoader) {
        if (!instanceMap.containsKey(classLoader)) {
            instanceMap.put(classLoader, new MutationPool());
        }

        return instanceMap.get(classLoader);
    }

    private MutationPool() {

    }

    // maps className -> method inside that class -> list of branches inside that method
    private final Map<String, Map<String, List<Mutation>>> mutationMap = new LinkedHashMap<>();

    // maps the mutationIDs assigned by this pool to their respective Mutations
    private final Map<Integer, Mutation> mutationIdMap = new LinkedHashMap<>();

    private int numMutations = 0;

    public Mutation addMutation(String className, String methodName,
                                String mutationName, BytecodeInstruction instruction,
                                AbstractInsnNode mutation, InsnList distance) {

        if (!mutationMap.containsKey(className))
            mutationMap.put(className, new HashMap<>());

        if (!mutationMap.get(className).containsKey(methodName))
            mutationMap.get(className).put(methodName, new ArrayList<>());

        Mutation mutationObject = new Mutation(className, methodName, mutationName,
                numMutations++, instruction, mutation, distance);
        mutationMap.get(className).get(methodName).add(mutationObject);
        mutationIdMap.put(mutationObject.getId(), mutationObject);

        return mutationObject;
    }

    public Mutation addMutation(String className, String methodName,
                                String mutationName, BytecodeInstruction instruction, InsnList mutation,
                                InsnList distance) {

        if (!mutationMap.containsKey(className))
            mutationMap.put(className, new HashMap<>());

        if (!mutationMap.get(className).containsKey(methodName))
            mutationMap.get(className).put(methodName, new ArrayList<>());

        Mutation mutationObject = new Mutation(className, methodName, mutationName,
                numMutations++, instruction, mutation, distance);
        mutationMap.get(className).get(methodName).add(mutationObject);

        mutationIdMap.put(mutationObject.getId(), mutationObject);

        return mutationObject;
    }

    /**
     * Returns a List containing all mutants in the given class and method
     * <p>
     * Should no such mutant exist an empty List is returned
     *
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<Mutation> retrieveMutationsInMethod(String className,
                                                    String methodName) {
        List<Mutation> r = new ArrayList<>();
        if (mutationMap.get(className) == null)
            return r;
        List<Mutation> mutants = mutationMap.get(className).get(methodName);
        if (mutants != null)
            r.addAll(mutants);
        return r;
    }

    /**
     * <p>getMutants</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Mutation> getMutants() {
        return new ArrayList<>(mutationIdMap.values());
    }

    public Mutation getMutant(int id) {
        return mutationIdMap.get(id);
    }

    /**
     * Remove all known mutants
     */
    public void clear() {
        mutationMap.clear();
        mutationIdMap.clear();
        numMutations = 0;
    }

    /**
     * Returns the number of currently known mutants
     *
     * @return The number of currently known mutants
     */
    public int getMutantCounter() {
        return numMutations;
    }
}
