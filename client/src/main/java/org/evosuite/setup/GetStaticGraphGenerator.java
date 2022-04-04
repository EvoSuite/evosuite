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

import org.evosuite.Properties;
import org.evosuite.instrumentation.BytecodeInstrumentation;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * This class creates a StaticUsageGraph by traversing the INVOKESTATIC/GETSTATIC relation
 * among instrumentable classes.
 *
 * @author Juan Galeotti
 */
public class GetStaticGraphGenerator {

    private static final Logger logger = LoggerFactory
            .getLogger(GetStaticGraphGenerator.class);

    public static GetStaticGraph generate(String className) {
        ClassNode targetClass = DependencyAnalysis.getClassNode(className);

        GetStaticGraph staticUsageTree = new GetStaticGraph();
        if (targetClass != null)
            handle(staticUsageTree, targetClass, 0);
        if (Properties.INSTRUMENT_PARENT) {
            handleSuperClasses(staticUsageTree, targetClass);
        }
        return staticUsageTree;
    }

    private static boolean isOverridden(String methodName) {
        return true;
    }

    /**
     * If we want to have the calltree also for the superclasses, we need to
     * determine which methods are callable
     *
     * @param staticUsageTree
     * @param targetClass
     */
    @SuppressWarnings("unchecked")
    private static void handleSuperClasses(GetStaticGraph staticUsageTree,
                                           ClassNode targetClass) {
        String superClassName = targetClass.superName;
        if (superClassName == null || superClassName.isEmpty())
            return;

        if (superClassName.equals("java/lang/Object"))
            return;

        logger.debug("Creating calltree for superclass: " + superClassName);
        ClassNode superClass = DependencyAnalysis.getClassNode(superClassName);
        List<MethodNode> methods = superClass.methods;
        for (MethodNode mn : methods) {
            logger.debug("Method: " + mn.name);

            // Do not check super-constructors
            if (mn.name.equals("<init>"))
                continue;
            if (mn.name.equals("<clinit>"))
                continue;

            // Skip abstract etc
            if ((mn.access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT)
                continue;

            // Do not handle classes if they are overridden by the subclass
            if ((mn.access & Opcodes.ACC_PUBLIC) == Opcodes.ACC_PUBLIC) {
                if (!isOverridden(mn.name + mn.desc)) {
                    handleMethodNode(staticUsageTree, superClass, mn, 0);
                }
            }
        }
        handleSuperClasses(staticUsageTree, superClass);

    }

    @SuppressWarnings("unchecked")
    private static void handle(GetStaticGraph staticUsageTree,
                               ClassNode targetClass, int depth) {
        List<MethodNode> methods = targetClass.methods;
        for (MethodNode mn : methods) {
            logger.debug("Method: " + mn.name);
            handleMethodNode(staticUsageTree, targetClass, mn, depth);
        }
    }

    @SuppressWarnings("unchecked")
    private static void handle(GetStaticGraph staticUsageTree,
                               ClassNode targetClass, String methodName, int depth) {
        List<MethodNode> methods = targetClass.methods;
        for (MethodNode mn : methods) {
            if (methodName.equals(mn.name + mn.desc))
                handleMethodNode(staticUsageTree, targetClass, mn, depth);
        }
    }

    private static void handle(GetStaticGraph staticUsageTree,
                               String className, String methodName, int depth) {
        ClassNode cn = DependencyAnalysis.getClassNode(className);
        if (cn == null)
            return;

        handle(staticUsageTree, cn, methodName, depth);
    }

    /**
     * Add all possible calls for a given method
     *
     * @param callGraph
     * @param mn
     */
    @SuppressWarnings("unchecked")
    private static void handleMethodNode(GetStaticGraph staticUsageTree,
                                         ClassNode cn, MethodNode mn, int depth) {

        InsnList instructions = mn.instructions;
        Iterator<AbstractInsnNode> iterator = instructions.iterator();

        // TODO: This really shouldn't be here but in its own class
        while (iterator.hasNext()) {
            AbstractInsnNode insn = iterator.next();
            if (insn instanceof MethodInsnNode) {
                handleMethodInsnNode(staticUsageTree, cn, mn,
                        (MethodInsnNode) insn, depth + 1);
            } else if (insn instanceof FieldInsnNode) {
                handleFieldInsnNode(staticUsageTree, cn, mn,
                        (FieldInsnNode) insn, depth + 1);
            }

        }
    }

    /**
     * Descend into a static field read
     */
    private static void handleFieldInsnNode(GetStaticGraph staticUsageTree,
                                            ClassNode cn, MethodNode mn, FieldInsnNode insn, int depth) {

        // Skip field instructions that are not reads to static fields
        if (insn.getOpcode() != Opcodes.GETSTATIC) {
            return;
        }

        // Only collect relations for instrumentable classes
        String calleeClassName = insn.owner.replaceAll("/", ".");
        if (BytecodeInstrumentation.checkIfCanInstrument(calleeClassName)) {
            logger.debug("Handling field read: " + insn.name);
            if (!staticUsageTree.hasStaticFieldRead(cn.name, mn.name + mn.desc,
                    insn.owner, insn.name)) {

                handleClassInitializer(staticUsageTree, cn, mn, insn.owner,
                        depth);

                // Add static read from mn to insn to static usage graph
                staticUsageTree.addStaticFieldRead(cn.name, mn.name + mn.desc,
                        insn.owner, insn.name);

                handle(staticUsageTree, insn.owner, insn.name + insn.desc,
                        depth);
            }
        }

    }

    /**
     * Descend into a static method call
     */
    private static void handleMethodInsnNode(GetStaticGraph staticUsageTree,
                                             ClassNode cn, MethodNode mn, MethodInsnNode methodCall, int depth) {

        // Skip if method call is not static
        if (methodCall.getOpcode() != Opcodes.INVOKESTATIC) {
            return;
        }

        // Only collect relations for instrumentable classes
        String calleeClassName = methodCall.owner.replaceAll("/", ".");
        if (BytecodeInstrumentation.checkIfCanInstrument(calleeClassName)) {
            logger.debug("Handling method: " + methodCall.name);
            handleClassInitializer(staticUsageTree, cn, mn, methodCall.owner,
                    depth);

            if (!staticUsageTree.hasStaticMethodCall(cn.name,
                    mn.name + mn.desc, methodCall.owner, methodCall.name
                            + methodCall.desc)) {

                // Add call from mn to methodCall to callgraph
                staticUsageTree.addStaticMethodCall(cn.name, mn.name + mn.desc,
                        methodCall.owner, methodCall.name + methodCall.desc);

                handle(staticUsageTree, methodCall.owner, methodCall.name
                        + methodCall.desc, depth);
            }
        }
    }

    private static final String CLINIT = "<clinit>";
    private static final String CLASS_INIT_NAME = CLINIT
            + Type.getMethodDescriptor(Type.VOID_TYPE);

    /**
     * Descend into a <clinit>
     */
    private static void handleClassInitializer(GetStaticGraph staticUsageTree,
                                               ClassNode cn, MethodNode mn, String owner, int depth) {
        if (!staticUsageTree.hasStaticMethodCall(cn.name, mn.name + mn.desc,
                owner, CLASS_INIT_NAME)) {

            // Add call from mn to methodCall to callgraph
            staticUsageTree.addStaticMethodCall(cn.name, mn.name + mn.desc,
                    owner, CLASS_INIT_NAME);

            // handle callee's <clinit>
            handle(staticUsageTree, owner, CLASS_INIT_NAME, depth);

        }
    }

}
