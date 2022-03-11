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
package org.evosuite.graphs;

import org.evosuite.Properties;
import org.evosuite.graphs.ccfg.ClassControlFlowGraph;
import org.evosuite.graphs.ccg.ClassCallGraph;
import org.evosuite.graphs.cdg.ControlDependenceGraph;
import org.evosuite.graphs.cfg.ActualControlFlowGraph;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.evosuite.setup.DependencyAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Gives access to all Graphs computed during CUT analysis such as CFGs created
 * by the CFGGenerator and BytcodeAnalyzer in the CFGMethodAdapter
 * <p>
 * For each CUT and each of their methods a Raw- and an ActualControlFlowGraph
 * instance are stored within this pool. Additionally a ControlDependenceGraph
 * is computed and stored for each such method.
 * <p>
 * This pool also offers the possibility to generate the ClassCallGraph and
 * ClassControlFlowGraph for a CUT. They represents the call hierarchy and
 * interaction of different methods within a class.
 *
 * @author Andre Mis
 */
public class GraphPool {

    private static final Logger logger = LoggerFactory.getLogger(GraphPool.class);

    private static final Map<ClassLoader, GraphPool> instanceMap = new HashMap<>();

    private final ClassLoader classLoader;

    /**
     * Private constructor
     */
    private GraphPool(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static GraphPool getInstance(ClassLoader classLoader) {
        if (!instanceMap.containsKey(classLoader)) {
            instanceMap.put(classLoader, new GraphPool(classLoader));
        }

        return instanceMap.get(classLoader);
    }

    /**
     * Complete control flow graph, contains each bytecode instruction, each
     * label and line number node Think of the direct Known Subclasses of
     * http://
     * asm.ow2.org/asm33/javadoc/user/org/objectweb/asm/tree/AbstractInsnNode
     * .html for a complete list of the nodes in this cfg
     * <p>
     * Maps from classNames to methodNames to corresponding RawCFGs
     */
    private final Map<String, Map<String, RawControlFlowGraph>> rawCFGs = new HashMap<>();

    /**
     * Minimized control flow graph. This graph only contains the first and last
     * node (usually a LABEL and IRETURN), nodes which create branches (all
     * jumps/switches except GOTO) and nodes which were mutated.
     * <p>
     * Maps from classNames to methodNames to corresponding ActualCFGs
     */
    private final Map<String, Map<String, ActualControlFlowGraph>> actualCFGs = new HashMap<>();

    /**
     * Control Dependence Graphs for each method.
     * <p>
     * Maps from classNames to methodNames to corresponding CDGs
     */
    private final Map<String, Map<String, ControlDependenceGraph>> controlDependencies = new HashMap<>();

    /**
     * Cache of all created CCFGs
     * <p>
     * Maps from classNames to computed CCFG of that class
     */
    private final Map<String, ClassControlFlowGraph> ccfgs = new HashMap<>();

    // retrieve graphs

    /**
     * Returns the {@link RawControlFlowGraph} of the specified method. To this end, one has to
     * provide
     * <ul>
     *     <li>the fully qualified name of the class containing the desired method, and</li>
     *     <li>a string consisting of the method name concatenated with the corresponding
     *     method descriptor.</li>
     * </ul>
     *
     * @param className  the fully qualified name of the containing class
     * @param methodName concatenation of method name and descriptor
     * @return the raw control flow graph
     */
    public RawControlFlowGraph getRawCFG(String className, String methodName) {

        if (rawCFGs.get(className) == null) {
            logger.warn("Class unknown: " + className);
            logger.warn(rawCFGs.keySet().toString());
            return null;
        }

        return rawCFGs.get(className).get(methodName);
    }

    /**
     * <p>
     * Getter for the field <code>rawCFGs</code>.
     * </p>
     *
     * @param className a {@link java.lang.String} object.
     * @return a {@link java.util.Map} object.
     */
    public Map<String, RawControlFlowGraph> getRawCFGs(String className) {
        if (rawCFGs.get(className) == null) {
            logger.warn("Class unknown: " + className);
            logger.warn(rawCFGs.keySet().toString());
            return null;
        }

        return rawCFGs.get(className);
    }

    /**
     * <p>
     * getActualCFG
     * </p>
     *
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @return a {@link org.evosuite.graphs.cfg.ActualControlFlowGraph} object.
     */
    public ActualControlFlowGraph getActualCFG(String className, String methodName) {

        if (actualCFGs.get(className) == null)
            return null;

        return actualCFGs.get(className).get(methodName);
    }

    /**
     * <p>
     * getCDG
     * </p>
     *
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     * @return a {@link org.evosuite.graphs.cdg.ControlDependenceGraph} object.
     */
    public ControlDependenceGraph getCDG(String className, String methodName) {

        if (controlDependencies.get(className) == null)
            return null;

        return controlDependencies.get(className).get(methodName);
    }

    // register graphs

    /**
     * <p>
     * registerRawCFG
     * </p>
     *
     * @param cfg a {@link org.evosuite.graphs.cfg.RawControlFlowGraph} object.
     */
    public void registerRawCFG(RawControlFlowGraph cfg) {
        String className = cfg.getClassName();
        String methodName = cfg.getMethodName();

        if (className == null || methodName == null)
            throw new IllegalStateException(
                    "expect class and method name of CFGs to be set before entering the GraphPool");

        if (!rawCFGs.containsKey(className)) {
            rawCFGs.put(className, new HashMap<>());
        }
        Map<String, RawControlFlowGraph> methods = rawCFGs.get(className);
        logger.debug("Added complete CFG for class " + className + " and method "
                + methodName);
        methods.put(methodName, cfg);

        if (Properties.WRITE_CFG)
            cfg.toDot();
    }

    /**
     * <p>
     * registerActualCFG
     * </p>
     *
     * @param cfg a {@link org.evosuite.graphs.cfg.ActualControlFlowGraph}
     *            object.
     */
    public void registerActualCFG(ActualControlFlowGraph cfg) {
        String className = cfg.getClassName();
        String methodName = cfg.getMethodName();

        if (className == null || methodName == null)
            throw new IllegalStateException(
                    "expect class and method name of CFGs to be set before entering the GraphPool");

        if (!actualCFGs.containsKey(className)) {
            actualCFGs.put(className, new HashMap<>());
            // diameters.put(className, new HashMap<String, Double>());
        }
        Map<String, ActualControlFlowGraph> methods = actualCFGs.get(className);
        logger.debug("Added CFG for class " + className + " and method " + methodName);
        cfg.finalise();
        methods.put(methodName, cfg);

        if (Properties.WRITE_CFG)
            cfg.toDot();

        if (DependencyAnalysis.shouldInstrument(cfg.getClassName(), cfg.getMethodName())) {
            createAndRegisterControlDependence(cfg);
        }
    }

    private void createAndRegisterControlDependence(ActualControlFlowGraph cfg) {

        ControlDependenceGraph cd = new ControlDependenceGraph(cfg);

        String className = cd.getClassName();
        String methodName = cd.getMethodName();

        if (className == null || methodName == null)
            throw new IllegalStateException(
                    "expect class and method name of CFGs to be set before entering the GraphPool");

        if (!controlDependencies.containsKey(className))
            controlDependencies.put(className,
                    new HashMap<>());
        Map<String, ControlDependenceGraph> cds = controlDependencies.get(className);

        cds.put(methodName, cd);
        if (Properties.WRITE_CFG)
            cd.toDot();
    }

    /**
     * Ensures this GraphPool knows the CCFG for the given class and then
     * returns it.
     *
     * @param className the name of the class of the CCFG as a
     *                  {@link java.lang.String}
     * @return The cached CCFG of type
     * {@link org.evosuite.graphs.ccfg.ClassControlFlowGraph}
     */
    public ClassControlFlowGraph getCCFG(String className) {
        if (!ccfgs.containsKey(className)) {
            ccfgs.put(className, computeCCFG(className));
        }

        return ccfgs.get(className);
    }

    public boolean canMakeCCFGForClass(String className) {
        //		if(!rawCFGs.containsKey(className))
        //			LoggingUtils.getEvoLogger().info("unable to create CCFG for "+className);
        return rawCFGs.containsKey(className);
    }

    /**
     * Computes the CCFG for the given class
     * <p>
     * If no CFG is known for the given class, an IllegalArgumentException is
     * thrown
     *
     * @param className a {@link java.lang.String} object.
     * @return a {@link org.evosuite.graphs.ccfg.ClassControlFlowGraph} object.
     */
    private ClassControlFlowGraph computeCCFG(String className) {
        if (rawCFGs.get(className) == null)
            throw new IllegalArgumentException(
                    "can't compute CCFG, don't know CFGs for class " + className);

        ClassCallGraph ccg = new ClassCallGraph(classLoader, className);
        if (Properties.WRITE_CFG)
            ccg.toDot();

        ClassControlFlowGraph ccfg = new ClassControlFlowGraph(ccg);
        if (Properties.WRITE_CFG)
            ccfg.toDot();

        return ccfg;
    }

    /**
     * <p>
     * clear
     * </p>
     */
    public void clear() {
        rawCFGs.clear();
        actualCFGs.clear();
        controlDependencies.clear();
    }

    /**
     * <p>
     * clear
     * </p>
     *
     * @param className a {@link java.lang.String} object.
     */
    public void clear(String className) {
        rawCFGs.remove(className);
        actualCFGs.remove(className);
        controlDependencies.remove(className);
    }

    /**
     * <p>
     * clear
     * </p>
     *
     * @param className  a {@link java.lang.String} object.
     * @param methodName a {@link java.lang.String} object.
     */
    public void clear(String className, String methodName) {
        if (rawCFGs.containsKey(className))
            rawCFGs.get(className).remove(methodName);
        if (actualCFGs.containsKey(className))
            actualCFGs.get(className).remove(methodName);
        if (controlDependencies.containsKey(className))
            controlDependencies.get(className).remove(methodName);
    }

    public static void clearAll(String className) {
        instanceMap.values().forEach(pool -> pool.clear(className));
    }

    public static void clearAll(String className, String methodName) {
        instanceMap.values().forEach(pool -> pool.clear(className, methodName));
    }

    public static void clearAll() {
        instanceMap.clear();
    }

}
