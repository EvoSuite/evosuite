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
package org.evosuite.graphs.ccg;

import org.evosuite.graphs.EvoSuiteGraph;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.RawControlFlowGraph;

import java.util.List;
import java.util.Map;

/**
 * Represents the method call structure of a class in a graph.
 * <p>
 * The graph contains a node for each of the classes methods with edges going
 * from a method node to each of its called methods.
 * <p>
 * Edges are labeled with the BytecodeInstruction of the corresponding call.
 *
 * @author Andre Mis
 */
public class ClassCallGraph extends EvoSuiteGraph<ClassCallNode, ClassCallEdge> {

    private final String className;

    private final ClassLoader classLoader;

    /**
     * @return the classLoader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * <p>
     * Constructor for ClassCallGraph.
     * </p>
     *
     * @param className a {@link java.lang.String} object.
     */
    public ClassCallGraph(ClassLoader classLoader, String className) {
        super(ClassCallEdge.class);

        this.className = className;
        this.classLoader = classLoader;

        compute();
    }

    private void compute() {
        Map<String, RawControlFlowGraph> cfgs = GraphPool.getInstance(classLoader).getRawCFGs(className);

        if (cfgs == null)
            throw new IllegalStateException(
                    "did not find CFGs for a class I was supposed to compute the CCG of");

        // add nodes
        for (String method : cfgs.keySet())
            addVertex(new ClassCallNode(method));

        //		System.out.println("generating class call graph for "+className);

        // add vertices
        for (ClassCallNode methodNode : graph.vertexSet()) {
            RawControlFlowGraph rcfg = cfgs.get(methodNode.getMethod());
            List<BytecodeInstruction> calls = rcfg.determineMethodCallsToOwnClass();
            //			System.out.println(calls.size()+" method calls from "+methodNode);
            for (BytecodeInstruction call : calls) {
                //				System.out.println("  to "+call.getCalledMethod()+" in "+call.getCalledMethodsClass());
                ClassCallNode calledMethod = getNodeByMethodName(call.getCalledMethod());
                if (calledMethod != null) {
                    ClassCallEdge e = new ClassCallEdge(call);
                    addEdge(methodNode, calledMethod, e);
                }
            }
        }
    }

    /**
     * <p>
     * getNodeByMethodName
     * </p>
     *
     * @param methodName a {@link java.lang.String} object.
     * @return a {@link org.evosuite.graphs.ccg.ClassCallNode} object.
     */
    public ClassCallNode getNodeByMethodName(String methodName) {
        ClassCallNode r = null;
        //		System.out.println("getting node by methodName "+methodName);
        for (ClassCallNode node : graph.vertexSet()) {
            if (node.getMethod().equals(methodName)) {
                if (r == null) {
                    r = node;
                } else {
                    throw new IllegalStateException(
                            "Expect each ClassCallNode to have a unique method name");
                }
            }
        }
        // TODO logger.warn
        //		if(r==null)
        //			System.out.println("didn't find node by methodName "+methodName);
        return r;
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

    // toDot util

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "CCG_" + className;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String dotSubFolder() {
        return toFileString(className) + "/";
    }
}
