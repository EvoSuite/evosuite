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
package org.evosuite.graphs.cfg;

import org.evosuite.graphs.EvoSuiteGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


/**
 * Abstract base class for both forms of CFGs inside EvoSuite
 * <p>
 * One implementation of this is cfg.RawControlFlowGraph, which is also known as
 * the complete CFG The other implementation of this is
 * cfg.ActualControlFlowGraph which is also known as the minimal CFG Look at the
 * respective classes for more detailed information
 * <p>
 * The CFGs can be accessed via the GraphPool which holds for each CUT and each
 * of their methods a complete and a minimal CFG
 * <p>
 * CFGs are created by the CFGGenerator during the analysis of the CUTs'
 * byteCode performed by the BytecodeAnalyzer
 *
 * @author Gordon Fraser, Andre Mis
 */
public abstract class ControlFlowGraph<V> extends
        EvoSuiteGraph<V, ControlFlowEdge> {

    private static final Logger logger = LoggerFactory
            .getLogger(ControlFlowGraph.class);

    protected String className;
    protected String methodName;
    protected int access;

    private int diameter = -1;

    /**
     * Creates a fresh and empty CFG for the given class and method
     *
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @param access     a int.
     */
    protected ControlFlowGraph(String className, String methodName, int access) {
        super(ControlFlowEdge.class);

        if (className == null || methodName == null)
            throw new IllegalArgumentException("null given");

        this.className = className;
        this.methodName = methodName;
        this.access = access;
    }

    /**
     * Creates a CFG determined by the given jGraph for the given class and
     * method
     *
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @param access     a int.
     * @param jGraph     a {@link org.jgrapht.graph.DefaultDirectedGraph} object.
     */
    protected ControlFlowGraph(String className, String methodName, int access,
                               DefaultDirectedGraph<V, ControlFlowEdge> jGraph) {
        super(jGraph, ControlFlowEdge.class);

        if (className == null || methodName == null)
            throw new IllegalArgumentException("null given");

        this.className = className;
        this.methodName = methodName;
        this.access = access;
    }

    /**
     * <p>leadsToNode</p>
     *
     * @param e a {@link org.evosuite.graphs.cfg.ControlFlowEdge} object.
     * @param b a V object.
     * @return a boolean.
     */
    public boolean leadsToNode(ControlFlowEdge e, V b) {

        Set<V> handled = new HashSet<>();

        Queue<V> queue = new LinkedList<>();
        queue.add(getEdgeTarget(e));
        while (!queue.isEmpty()) {
            V current = queue.poll();
            if (handled.contains(current))
                continue;
            handled.add(current);

            for (V next : getChildren(current))
                if (next.equals(b))
                    return true;
                else
                    queue.add(next);
        }

        return false;
    }

    // /**
    // * Can be used to retrieve a Branch contained in this CFG identified by
    // it's
    // * branchId
    // *
    // * If no such branch exists in this CFG, null is returned
    // */
    // public abstract BytecodeInstruction getBranch(int branchId);

    /**
     * Can be used to retrieve an instruction contained in this CFG identified
     * by it's instructionId
     * <p>
     * If no such instruction exists in this CFG, null is returned
     *
     * @param instructionId a int.
     * @return a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     */
    public abstract BytecodeInstruction getInstruction(int instructionId);

    /**
     * Determines, whether a given instruction is contained in this CFG
     *
     * @param instruction a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @return a boolean.
     */
    public abstract boolean containsInstruction(BytecodeInstruction instruction);

    /**
     * Computes the diameter of this CFG and the mutation distances
     * <p>
     * Since both takes some time this is not automatically done on each CFG
     * <p>
     * GraphPool will automatically call this immediately after the
     * instantiation of an ActualControlFlowGraph, but not after the creation of
     * a RawControlFlowGraph
     */
    public void finalise() {
        computeDiameter();
        // TODO: call this!
        // and sanity check with a flag whenever a call
        // to this method is assumed to have been made
    }

    /**
     * Returns the Diameter of this CFG
     * <p>
     * If the diameter of this graph was not computed previously it is computed
     * first
     *
     * @return a int.
     */
    public int getDiameter() {
        if (diameter == -1) {
            logger.debug("diameter not computed yet. calling computeDiameter() first!");
            computeDiameter();
        }

        return diameter;
    }

    public int getCyclomaticComplexity() {
        // E = the number of edges of the graph.
        // N = the number of nodes of the graph.
        // M = E − N + 2
        int E = this.edgeCount();
        int N = this.vertexCount();
        return E - N + 2;
    }

    /**
     * <p>computeDiameter</p>
     */
    protected void computeDiameter() {
        // The diameter is just an upper bound for the approach level
        // Let's try to use something that's easier to compute than
        // FLoydWarshall
        diameter = this.edgeCount();
        /*
         * FloydWarshall<V, ControlFlowEdge> f = new FloydWarshall<V,
         * ControlFlowEdge>(graph); diameter = (int) f.getDiameter();
         */
    }

    /**
     * <p>determineEntryPoint</p>
     *
     * @return a V object.
     */
    public V determineEntryPoint() {
        Set<V> candidates = determineEntryPoints();

        if (candidates.size() > 1)
            throw new IllegalStateException(
                    "expect CFG of a method to contain at most one instruction with no parent in "
                            + methodName);

        for (V instruction : candidates)
            return instruction;

        // there was a back loop to the first instruction within this CFG, so no
        // candidate
        // TODO for now return null and handle in super class
        // RawControlFlowGraph separately by overwriting this method

        // can also happen in empty methods
        return null;
    }

    /**
     * <p>Getter for the field <code>className</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassName() {
        return className;
    }

    /**
     * <p>Getter for the field <code>methodName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * <p>getMethodAccess</p>
     *
     * @return a int.
     */
    public int getMethodAccess() {
        return access;
    }

    /**
     * <p>isPublicMethod</p>
     *
     * @return a boolean.
     */
    public boolean isPublicMethod() {
        return (access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC;
    }

    /**
     * <p>isStaticMethod</p>
     *
     * @return a boolean.
     */
    public boolean isStaticMethod() {
        return (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return methodName + " " + getCFGType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String dotSubFolder() {
        return toFileString(className) + "/" + getCFGType() + "/";
    }

    /**
     * <p>getCFGType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getCFGType();
}
