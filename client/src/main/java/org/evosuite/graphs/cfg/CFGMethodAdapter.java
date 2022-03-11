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

import org.evosuite.Properties;
import org.evosuite.Properties.Criterion;
import org.evosuite.coverage.branch.BranchPool;
import org.evosuite.instrumentation.coverage.BranchInstrumentation;
import org.evosuite.instrumentation.coverage.DefUseInstrumentation;
import org.evosuite.instrumentation.coverage.MethodInstrumentation;
import org.evosuite.instrumentation.coverage.MutationInstrumentation;
import org.evosuite.runtime.annotation.EvoSuiteExclude;
import org.evosuite.runtime.classhandling.ClassResetter;
import org.evosuite.runtime.instrumentation.AnnotatedMethodNode;
import org.evosuite.setup.DependencyAnalysis;
import org.evosuite.utils.ArrayUtil;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Create a minimized control flow graph for the method and store it. In
 * addition, this adapter also adds instrumentation for branch distance
 * measurement
 * <p>
 * defUse, concurrency and LCSAJs instrumentation is also added (if the
 * properties are set).
 *
 * @author Gordon Fraser
 */
public class CFGMethodAdapter extends MethodVisitor {

    private static final Logger logger = LoggerFactory.getLogger(CFGMethodAdapter.class);

    /**
     * A list of Strings representing method signatures. Methods matching those
     * signatures are not instrumented and no CFG is generated for them. Except
     * if some MethodInstrumentation requests it.
     */
    public static final List<String> EXCLUDE = Arrays.asList("<clinit>()V",
            ClassResetter.STATIC_RESET + "()V",
            ClassResetter.STATIC_RESET);
    /**
     * The set of all methods which can be used during test case generation This
     * excludes e.g. synthetic, initializers, private and deprecated methods
     */
    public static Map<ClassLoader, Map<String, Set<String>>> methods = new HashMap<>();

    /**
     * This is the name + the description of the method. It is more like the
     * signature and less like the name. The name of the method can be found in
     * this.plain_name
     */
    private final String methodName;

    private final MethodVisitor next;
    private final String plain_name;
    private final int access;
    private final String className;
    private final ClassLoader classLoader;

    private int lineNumber = 0;

    /**
     * Can be set by annotation
     */
    private boolean excludeMethod = false;

    /**
     * <p>
     * Constructor for CFGMethodAdapter.
     * </p>
     *
     * @param className  a {@link java.lang.String} object.
     * @param access     a int.
     * @param name       a {@link java.lang.String} object.
     * @param desc       a {@link java.lang.String} object.
     * @param signature  a {@link java.lang.String} object.
     * @param exceptions an array of {@link java.lang.String} objects.
     * @param mv         a {@link org.objectweb.asm.MethodVisitor} object.
     */
    public CFGMethodAdapter(ClassLoader classLoader, String className, int access,
                            String name, String desc, String signature, String[] exceptions,
                            MethodVisitor mv) {

        // super(new MethodNode(access, name, desc, signature, exceptions),
        // className,
        // name.replace('/', '.'), null, desc);

        super(Opcodes.ASM9, new AnnotatedMethodNode(access, name, desc, signature,
                exceptions));

        this.next = mv;
        this.className = className; // .replace('/', '.');
        this.access = access;
        this.methodName = name + desc;
        this.plain_name = name;
        this.classLoader = classLoader;

        if (!methods.containsKey(classLoader))
            methods.put(classLoader, new HashMap<>());
    }

    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodVisitor#visitLineNumber(int, org.objectweb.asm.Label)
     */
    @Override
    public void visitLineNumber(int line, Label start) {
        lineNumber = line;
        super.visitLineNumber(line, start);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (Type.getDescriptor(EvoSuiteExclude.class).equals(desc)) {
            logger.info("Method has EvoSuite annotation: " + desc);
            excludeMethod = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitEnd() {
        logger.debug("Creating CFG of " + className + "." + methodName);
        boolean isExcludedMethod = excludeMethod || EXCLUDE.contains(methodName);
        boolean isMainMethod = plain_name.equals("main") && Modifier.isStatic(access);

        List<MethodInstrumentation> instrumentations = new ArrayList<>();
        if (DependencyAnalysis.shouldInstrument(className, methodName)) {
            if (ArrayUtil.contains(Properties.CRITERION, Criterion.DEFUSE)
                    || ArrayUtil.contains(Properties.CRITERION, Criterion.ALLDEFS)) {
                instrumentations.add(new BranchInstrumentation());
                instrumentations.add(new DefUseInstrumentation());
            } else if (ArrayUtil.contains(Properties.CRITERION, Criterion.MUTATION)
                    || ArrayUtil.contains(Properties.CRITERION, Criterion.WEAKMUTATION)
                    || ArrayUtil.contains(Properties.CRITERION, Criterion.ONLYMUTATION)
                    || ArrayUtil.contains(Properties.CRITERION, Criterion.STRONGMUTATION)) {
                instrumentations.add(new BranchInstrumentation());
                instrumentations.add(new MutationInstrumentation());
            } else {
                instrumentations.add(new BranchInstrumentation());
            }
        } else {
            //instrumentations.add(new BranchInstrumentation());
        }

        boolean executeOnMain = false;
        boolean executeOnExcluded = false;

        for (MethodInstrumentation instrumentation : instrumentations) {
            executeOnMain = executeOnMain || instrumentation.executeOnMainMethod();
            executeOnExcluded = executeOnExcluded
                    || instrumentation.executeOnExcludedMethods();
        }

        // super.visitEnd();
        // Generate CFG of method
        MethodNode mn = (AnnotatedMethodNode) mv;

        boolean checkForMain = false;
        if (Properties.CONSIDER_MAIN_METHODS) {
            checkForMain = true;
        } else {
            checkForMain = !isMainMethod || executeOnMain;
        }

        // Only instrument if the method is (not main and not excluded) or (the
        // MethodInstrumentation wants it anyway)
        if (checkForMain && (!isExcludedMethod || executeOnExcluded)
                && (access & Opcodes.ACC_ABSTRACT) == 0
                && (access & Opcodes.ACC_NATIVE) == 0) {

            logger.info("Analyzing method " + methodName + " in class " + className);

            // MethodNode mn = new CFGMethodNode((MethodNode)mv);
            // System.out.println("Generating CFG for "+ className+"."+mn.name +
            // " ("+mn.desc +")");

            BytecodeAnalyzer bytecodeAnalyzer = new BytecodeAnalyzer();
            logger.info("Generating CFG for method " + methodName);

            try {

                bytecodeAnalyzer.analyze(classLoader, className, methodName, mn);
                logger.trace("Method graph for "
                        + className
                        + "."
                        + methodName
                        + " contains "
                        + bytecodeAnalyzer.retrieveCFGGenerator().getRawGraph().vertexSet().size()
                        + " nodes for " + bytecodeAnalyzer.getFrames().length
                        + " instructions");
                // compute Raw and ActualCFG and put both into GraphPool
                bytecodeAnalyzer.retrieveCFGGenerator().registerCFGs();
                logger.info("Created CFG for method " + methodName);

                if (DependencyAnalysis.shouldInstrument(className, methodName)) {
                    if (!methods.get(classLoader).containsKey(className))
                        methods.get(classLoader).put(className, new HashSet<>());

                    // add the actual instrumentation
                    logger.info("Instrumenting method " + methodName + " in class "
                            + className);
                    for (MethodInstrumentation instrumentation : instrumentations)
                        instrumentation.analyze(classLoader, mn, className, methodName, access);

                    handleBranchlessMethods();
                    String id = className + "." + methodName;
                    if (isUsable()) {
                        methods.get(classLoader).get(className).add(id);
                        logger.debug("Counting: " + id);
                    }
                }
            } catch (AnalyzerException e) {
                logger.error("Analyzer exception while analyzing " + className + "."
                        + methodName + ": " + e);
                e.printStackTrace();
            }

        } else {
            logger.debug("NOT Creating CFG of " + className + "." + methodName + ": " + checkForMain + ", " + ((!isExcludedMethod || executeOnExcluded)) + ", " + ((access & Opcodes.ACC_ABSTRACT) == 0) + ", " + ((access & Opcodes.ACC_NATIVE) == 0));
            super.visitEnd();
        }
        mn.accept(next);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.commons.LocalVariablesSorter#visitMaxs(int, int)
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        int maxNum = 7;
        super.visitMaxs(Math.max(maxNum, maxStack), maxLocals);
    }

    private void handleBranchlessMethods() {
        String id = className + "." + methodName;
        if (BranchPool.getInstance(classLoader).getNonArtificialBranchCountForMethod(className, methodName) == 0) {
            if (isUsable()) {
                logger.debug("Method has no branches: " + id);
                BranchPool.getInstance(classLoader).addBranchlessMethod(className, id, lineNumber);
            }
        }
    }

    /**
     * See description of CFGMethodAdapter.EXCLUDE
     *
     * @return
     */
    private boolean isUsable() {
        if ((this.access & Opcodes.ACC_SYNTHETIC) != 0)
            return false;

        if ((this.access & Opcodes.ACC_BRIDGE) != 0)
            return false;

        if ((this.access & Opcodes.ACC_NATIVE) != 0)
            return false;

        if (methodName.contains("<clinit>"))
            return false;

        // If we are not using reflection, covering private constructors is difficult?
        if (Properties.P_REFLECTION_ON_PRIVATE <= 0.0) {
            return !methodName.contains("<init>") || (access & Opcodes.ACC_PRIVATE) != Opcodes.ACC_PRIVATE;
        }

        return true;
    }

    public Set<String> getMethods(String className) {
        return getMethods(classLoader, className);
    }

    /**
     * Returns a set with all unique methodNames of methods.
     *
     * @param className a {@link java.lang.String} object.
     * @return A set with all unique methodNames of methods.
     */
    public static Set<String> getMethods(ClassLoader classLoader, String className) {
        Set<String> targetMethods = new HashSet<>();
        if (!methods.containsKey(classLoader))
            return targetMethods;

        for (String currentClass : methods.get(classLoader).keySet()) {
            if (currentClass.equals(className)
                    || currentClass.startsWith(className + "$"))
                targetMethods.addAll(methods.get(classLoader).get(currentClass));
        }

        return targetMethods;
    }

    public Set<String> getMethods() {
        return getMethods(classLoader);
    }

    /**
     * Returns a set with all unique methodNames of methods.
     *
     * @return A set with all unique methodNames of methods.
     */
    public static Set<String> getMethods(ClassLoader classLoader) {
        Set<String> targetMethods = new HashSet<>();
        if (!methods.containsKey(classLoader))
            return targetMethods;

        for (String currentClass : methods.get(classLoader).keySet()) {
            targetMethods.addAll(methods.get(classLoader).get(currentClass));
        }

        return targetMethods;
    }


    public Set<String> getMethodsPrefix(String className) {
        return getMethodsPrefix(classLoader, className);
    }

    /**
     * Returns a set with all unique methodNames of methods.
     *
     * @param className a {@link java.lang.String} object.
     * @return A set with all unique methodNames of methods.
     */
    public static Set<String> getMethodsPrefix(ClassLoader classLoader, String className) {
        Set<String> matchingMethods = new HashSet<>();
        if (!methods.containsKey(classLoader))
            return matchingMethods;

        for (String name : methods.get(classLoader).keySet()) {
            if (name.startsWith(className)) {
                matchingMethods.addAll(methods.get(classLoader).get(name));
            }
        }

        return matchingMethods;
    }

    public int getNumMethodsPrefix(String className) {
        return getNumMethodsPrefix(classLoader, className);
    }

    /**
     * Returns a set with all unique methodNames of methods.
     *
     * @param className a {@link java.lang.String} object.
     * @return A set with all unique methodNames of methods.
     */
    public static int getNumMethodsPrefix(ClassLoader classLoader, String className) {
        int num = 0;
        if (!methods.containsKey(classLoader))
            return num;

        for (String name : methods.get(classLoader).keySet()) {
            if (name.startsWith(className)) {
                num += methods.get(classLoader).get(name).size();
            }
        }

        return num;
    }

    public int getNumMethods() {
        return getNumMethods(classLoader);
    }

    /**
     * Returns a set with all unique methodNames of methods.
     *
     * @return A set with all unique methodNames of methods.
     */
    public static int getNumMethods(ClassLoader classLoader) {
        int num = 0;
        if (!methods.containsKey(classLoader))
            return num;

        for (String name : methods.get(classLoader).keySet()) {
            num += methods.get(classLoader).get(name).size();
        }

        return num;
    }


    public int getNumMethodsMemberClasses(String className) {
        return getNumMethodsMemberClasses(classLoader, className);
    }

    /**
     * Returns a set with all unique methodNames of methods.
     *
     * @param className a {@link java.lang.String} object.
     * @return A set with all unique methodNames of methods.
     */
    public static int getNumMethodsMemberClasses(ClassLoader classLoader, String className) {
        int num = 0;
        if (!methods.containsKey(classLoader))
            return num;

        for (String name : methods.get(classLoader).keySet()) {
            if (name.equals(className) || name.startsWith(className + "$")) {
                num += methods.get(classLoader).get(name).size();
            }
        }

        return num;
    }
}
