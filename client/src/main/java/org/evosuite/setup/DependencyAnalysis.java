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

package org.evosuite.setup;

import org.evosuite.PackageInfo;
import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.TestGenerationContext;
import org.evosuite.classpath.ResourceList;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.coverage.dataflow.DefUsePool;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.graphs.cfg.CFGMethodAdapter;
import org.evosuite.instrumentation.LinePool;
import org.evosuite.junit.CoverageAnalysis;
import org.evosuite.rmi.ClientServices;
import org.evosuite.setup.callgraph.CallGraph;
import org.evosuite.setup.callgraph.CallGraphGenerator;
import org.evosuite.statistics.RuntimeVariable;
import org.evosuite.utils.ArrayUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * This class performs static analysis before everything else initializes
 *
 * @author Gordon Fraser
 */
public class DependencyAnalysis {

    private static final Logger logger = LoggerFactory.getLogger(DependencyAnalysis.class);

    private static final Map<String, ClassNode> classCache = new LinkedHashMap<>();

    private static final Map<String, CallGraph> callGraphs = new LinkedHashMap<>();

    private static InheritanceTree inheritanceTree = null;

    private static Set<String> targetClasses = null;

    /**
     * @return the inheritanceTree
     */
    public static InheritanceTree getInheritanceTree() {
        return inheritanceTree;
    }

    public static void initInheritanceTree(List<String> classPath) {
        if (inheritanceTree == null) {
            logger.debug("Calculate inheritance hierarchy");
            inheritanceTree = InheritanceTreeGenerator.createFromClassPath(classPath);
        }
        TestClusterGenerator clusterGenerator = new TestClusterGenerator(inheritanceTree);
        TestGenerationContext.getInstance().setTestClusterGenerator(clusterGenerator);
        InheritanceTreeGenerator.gatherStatistics(inheritanceTree);
    }

    public static void initCallGraph(String className) {
        logger.debug("Calculate call tree");
        CallGraph callGraph = CallGraphGenerator.analyze(className);
        callGraphs.put(className, callGraph);
        // include all the project classes in the inheritance tree and in the callgraph.
        if (ArrayUtil.contains(Properties.CRITERION, Criterion.IBRANCH)
                || Properties.INSTRUMENT_CONTEXT) {

            for (String classn : inheritanceTree.getAllClasses()) {
                if (isTargetProject(classn)) {
                    CallGraphGenerator.analyzeOtherClasses(callGraph, classn);
                }
            }
        }

        // TODO: Need to make sure that all classes in calltree are instrumented
        logger.debug("Update call tree with calls to overridden methods");
        CallGraphGenerator.update(callGraph, inheritanceTree);

    }

    private static void analyze(String className) throws RuntimeException,
            ClassNotFoundException {

        if (!inheritanceTree.hasClass(Properties.TARGET_CLASS)) {
            throw new ClassNotFoundException("Target class not found in inheritance tree");
        }

        CallGraph callGraph = callGraphs.get(className);
        loadCallTreeClasses(callGraph);

        logger.debug("Create test cluster");

        // if a class is not instrumented but part of the callgraph, the
        // generateCluster method will instrument it
        // update: we instrument only classes reachable from the class
        // under test, the callgraph is populated with all classes, but only the
        // set of relevant ones are instrumented - mattia
        TestGenerationContext.getInstance().getTestClusterGenerator().generateCluster(callGraph);

        gatherStatistics();
    }

    /**
     * Start analysis from target class
     *
     * @param className
     */
    public static void analyzeClass(String className, List<String> classPath) throws RuntimeException,
            ClassNotFoundException {

        initInheritanceTree(classPath);
        initCallGraph(className);

        analyze(className);
    }

    /**
     * Start analysis from target
     *
     * @param target (e.g., directory, or jar file)
     */
    public static Set<String> analyzeTarget(String target, List<String> classPath) throws RuntimeException,
            ClassNotFoundException {

        initInheritanceTree(classPath);

        targetClasses = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getAllClasses(target, false);
        for (String className : targetClasses) {
            Properties.TARGET_CLASS = className;
            initCallGraph(className);
            analyze(className);
        }

        return targetClasses;
    }

    private static void loadCallTreeClasses(CallGraph callGraph) {
        for (String className : callGraph.getClasses()) {
            if (className.startsWith(Properties.TARGET_CLASS + "$")) {
                try {
                    Class.forName(className, true,
                            TestGenerationContext.getInstance().getClassLoaderForSUT());
                } catch (ClassNotFoundException e) {
                    logger.debug("Error loading " + className + ": " + e);
                }
            }
        }
    }

    /**
     * @param className
     * @return the CallGraph of className
     */
    public static CallGraph getCallGraph(String className) {
        return callGraphs.get(className);
    }

    /**
     * @return the CallGraph of Properties.TARGET_CLASS
     */
    public static CallGraph getCallGraph() {
        return callGraphs.get(Properties.TARGET_CLASS);
    }

    /**
     * Determine if the given class is the target class
     *
     * @param className
     * @return
     */
    public static boolean isTargetClassName(String className) {
        if (!Properties.TARGET_CLASS_PREFIX.isEmpty()
                && className.startsWith(Properties.TARGET_CLASS_PREFIX)) {
            // exclude existing tests from the target project
            try {
                Class<?> clazz = Class.forName(className);
                return !CoverageAnalysis.isTest(clazz);
            } catch (ClassNotFoundException e) {
                logger.info("Could not find class " + className);
            }
        }
        if (className.equals(Properties.TARGET_CLASS)
                || className.startsWith(Properties.TARGET_CLASS + "$")) {
            return true;
        }
        return targetClasses != null && targetClasses.contains(className);
    }

    // TODO implement something that takes parameters using properties -
    // generalize this method.
    public static boolean isTargetProject(String className) {
        return (className.startsWith(Properties.PROJECT_PREFIX) || (!Properties.TARGET_CLASS_PREFIX
                .isEmpty() && className.startsWith(Properties.TARGET_CLASS_PREFIX)))
                && !className.startsWith("java.")
                && !className.startsWith("sun.")
                && !className.startsWith(PackageInfo.getEvoSuitePackage())
                && !className.startsWith("org.exsyst")
                && !className.startsWith("de.unisb.cs.st.evosuite")
                && !className.startsWith("de.unisb.cs.st.specmate")
                && !className.startsWith("javax.")
                && !className.startsWith("org.xml")
                && !className.startsWith("org.w3c")
                && !className.startsWith("apple.")
                && !className.startsWith("com.apple.")
                && !className.startsWith("org.omg.")
                && !className.startsWith("sunw.")
                && !className.startsWith("org.jcp.")
                && !className.startsWith("org.ietf.")
                && !className.startsWith("daikon.")
                && !className.startsWith("jdk.");
    }

    /**
     * Determine if the given class should be analyzed or instrumented
     *
     * @param className
     * @return
     */
    public static boolean shouldAnalyze(String className) {
        // Always analyze if it is a target class
        if (isTargetClassName(className))
            return true;

        if (inheritanceTree == null) {
            return false;
        }
        // Also analyze if it is a superclass and instrument_parent = true
        if (Properties.INSTRUMENT_PARENT) {
            if (inheritanceTree.getSuperclasses(Properties.TARGET_CLASS).contains(className))
                return true;
        }

        // Also analyze if it is in the calltree and we are considering the
        // context
        if (Properties.INSTRUMENT_CONTEXT
                || ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)
                || ArrayUtil.contains(Properties.CRITERION, Criterion.IBRANCH)) {
            CallGraph callGraph = callGraphs.get(Properties.TARGET_CLASS);
            return callGraph != null && callGraph.isCalledClass(className);
        }

        return false;
    }

    /**
     * Determine if the given method should be instrumented
     *
     * @param className
     * @param methodName
     * @return
     */
    public static boolean shouldInstrument(String className, String methodName) {
        // Always analyze if it is a target class
        if (isTargetClassName(className))
            return true;

        // Also analyze if it is a superclass and instrument_parent = true
        if (Properties.INSTRUMENT_PARENT) {
            if (inheritanceTree.getSuperclasses(Properties.TARGET_CLASS).contains(className))
                return true;
        }

        // Also analyze if it is in the calltree and we are considering the
        // context
        if (Properties.INSTRUMENT_CONTEXT) {

            CallGraph callGraph = callGraphs.get(Properties.TARGET_CLASS);
            if (callGraph != null && callGraph.isCalledMethod(className, methodName)) {
                return Properties.INSTRUMENT_LIBRARIES || DependencyAnalysis.isTargetProject(className);
            }
        }

        return false;
    }

    public static ClassNode getClassNode(String className) {
        if (!classCache.containsKey(className)) {
            try {
                classCache.put(className, loadClassNode(className));
            } catch (IOException e) {
                classCache.put(className, null);
            }
        }

        return classCache.get(className);

    }

    public static Collection<ClassNode> getAllClassNodes() {
        return classCache.values();
    }

    private static ClassNode loadClassNode(String className) throws IOException {

        InputStream classStream = ResourceList.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getClassAsStream(className);
        if (classStream == null) {
            // This used to throw an IOException that leads to null being
            // returned, so for now we're just returning null directly
            // TODO: Proper treatment of missing classes (can also be
            //       invalid calls, e.g. [L/java/lang/Object;)
            logger.info("Could not find class file: " + className);
            return null;
        }
        ClassNode cn = new ClassNode();
        try {
            ClassReader reader = new ClassReader(classStream);
            reader.accept(cn, ClassReader.SKIP_FRAMES); // |
            // ClassReader.SKIP_DEBUG);
        } finally {
            classStream.close(); // ASM does not close the stream
        }
        return cn;
    }


    private static void gatherStatistics() {
        ClientServices.getInstance().getClientNode()
                .trackOutputVariable(RuntimeVariable.Predicates, BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCounter());
        ClientServices.getInstance().getClientNode()
                .trackOutputVariable(RuntimeVariable.Instrumented_Predicates, BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getNumArtificialBranches());
        int numBranches = BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCounter() * 2;
        ClientServices
                .getInstance()
                .getClientNode()
                .trackOutputVariable(RuntimeVariable.Total_Branches, numBranches);
        ClientServices
                .getInstance()
                .getClientNode()
                .trackOutputVariable(RuntimeVariable.Total_Branches_Real,
                        ((BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchCounter() - BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getNumArtificialBranches())) * 2);
        ClientServices
                .getInstance()
                .getClientNode()
                .trackOutputVariable(RuntimeVariable.Total_Branches_Instrumented,
                        (BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getNumArtificialBranches() * 2));
        ClientServices
                .getInstance()
                .getClientNode()
                .trackOutputVariable(RuntimeVariable.Branchless_Methods,
                        BranchPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getBranchlessMethods().size());
        ClientServices
                .getInstance()
                .getClientNode()
                .trackOutputVariable(RuntimeVariable.Total_Methods,
                        CFGMethodAdapter.getNumMethods(TestGenerationContext.getInstance().getClassLoaderForSUT()));

        ClientServices.getInstance().getClientNode()
                .trackOutputVariable(RuntimeVariable.Lines, LinePool.getNumLines());

        for (Properties.Criterion pc : Properties.CRITERION) {
            switch (pc) {
                case DEFUSE:
                case ALLDEFS:
                    ClientServices
                            .getInstance()
                            .getClientNode()
                            .trackOutputVariable(RuntimeVariable.Definitions,
                                    DefUsePool.getDefCounter());
                    ClientServices.getInstance().getClientNode()
                            .trackOutputVariable(RuntimeVariable.Uses, DefUsePool.getUseCounter());
                    break;

                case WEAKMUTATION:
                case STRONGMUTATION:
                case MUTATION:
                    ClientServices
                            .getInstance()
                            .getClientNode()
                            .trackOutputVariable(RuntimeVariable.Mutants,
                                    MutationPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getMutantCounter());
                    break;

                default:
                    break;
            }
        }
    }
}
