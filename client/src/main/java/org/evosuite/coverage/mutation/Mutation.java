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

import org.evosuite.coverage.branch.BranchCoverageGoal;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.ControlDependency;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * Mutation class.
 * </p>
 *
 * @author Gordon Fraser
 */
public class Mutation implements Comparable<Mutation> {

    private final int id;

    private final String className;

    private final String methodName;

    private final String mutationName;

    private final BytecodeInstruction original;

    private final InsnList mutation;

    private final InsnList infection;

    private final int lineNo;

    /**
     * <p>
     * Constructor for Mutation.
     * </p>
     *
     * @param className    a {@link java.lang.String} object.
     * @param methodName   a {@link java.lang.String} object.
     * @param mutationName a {@link java.lang.String} object.
     * @param id           a int.
     * @param original     a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param mutation     a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     * @param distance     a {@link org.objectweb.asm.tree.InsnList} object.
     */
    public Mutation(String className, String methodName, String mutationName, int id,
                    BytecodeInstruction original, AbstractInsnNode mutation, InsnList distance) {
        this.className = className;
        this.methodName = methodName;
        this.mutationName = mutationName;
        this.id = id;
        this.original = original;
        this.mutation = new InsnList();
        this.mutation.add(mutation);
        this.infection = distance;
        this.lineNo = original.getLineNumber();
    }

    /**
     * <p>
     * Constructor for Mutation.
     * </p>
     *
     * @param className    a {@link java.lang.String} object.
     * @param methodName   a {@link java.lang.String} object.
     * @param mutationName a {@link java.lang.String} object.
     * @param id           a int.
     * @param original     a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param mutation     a {@link org.objectweb.asm.tree.InsnList} object.
     * @param distance     a {@link org.objectweb.asm.tree.InsnList} object.
     */
    public Mutation(String className, String methodName, String mutationName, int id,
                    BytecodeInstruction original, InsnList mutation, InsnList distance) {
        this.className = className;
        this.methodName = methodName;
        this.mutationName = mutationName;
        this.id = id;
        this.original = original;
        this.mutation = mutation;
        this.infection = distance;
        this.lineNo = original.getLineNumber();
    }

    /**
     * <p>
     * Getter for the field <code>id</code>.
     * </p>
     *
     * @return a int.
     */
    public int getId() {
        return id;
    }

    /**
     * <p>
     * Getter for the field <code>className</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassName() {
        return className;
    }

    /**
     * <p>
     * Getter for the field <code>methodName</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMethodName() {
        return methodName;
    }

    public int getLineNumber() {
        return lineNo;
    }

    /**
     * <p>
     * getOperandSize
     * </p>
     *
     * @return a int.
     */
    public int getOperandSize() {
        return 0;
    }

    /**
     * <p>
     * Getter for the field <code>mutationName</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMutationName() {
        return mutationName + " (" + id + "): " + ", line " + original.getLineNumber();
    }

    /**
     * <p>
     * getControlDependencies
     * </p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<BranchCoverageGoal> getControlDependencies() {
        Set<BranchCoverageGoal> goals = new HashSet<>();
        for (ControlDependency cd : original.getControlDependencies()) {
            BranchCoverageGoal goal = new BranchCoverageGoal(cd, className, methodName);
            goals.add(goal);
        }
        return goals;
    }

    /**
     * <p>
     * getOriginalNode
     * </p>
     *
     * @return a {@link org.objectweb.asm.tree.AbstractInsnNode} object.
     */
    public AbstractInsnNode getOriginalNode() {
        return original.getASMNode();
    }

    /**
     * <p>
     * Getter for the field <code>mutation</code>.
     * </p>
     *
     * @return a {@link org.objectweb.asm.tree.InsnList} object.
     */
    public InsnList getMutation() {
        return mutation;
    }

    /**
     * <p>
     * getInfectionDistance
     * </p>
     *
     * @return a {@link org.objectweb.asm.tree.InsnList} object.
     */
    public InsnList getInfectionDistance() {
        return infection;
    }

    /**
     * <p>
     * getDefaultInfectionDistance
     * </p>
     *
     * @return a {@link org.objectweb.asm.tree.InsnList} object.
     */
    public static InsnList getDefaultInfectionDistance() {
        InsnList defaultDistance = new InsnList();
        defaultDistance.add(new LdcInsnNode(0.0));
        return defaultDistance;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Mutation " + id + ": " + className + "." + methodName + ":" + lineNo
                + " - " + mutationName;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + id;
        result = prime * result + lineNo;
        result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result + ((mutationName == null) ? 0 : mutationName.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Mutation other = (Mutation) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (id != other.id)
            return false;
        if (lineNo != other.lineNo)
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        if (mutationName == null) {
            return other.mutationName == null;
        } else return mutationName.equals(other.mutationName);
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Mutation o) {
        return lineNo - o.lineNo;
    }

}
