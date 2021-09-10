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

package org.evosuite.instrumentation.testability;

import org.evosuite.instrumentation.TransformationStatistics;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;

/**
 * <p>
 * StringTransformation class.
 * </p>
 *
 * @author fraser
 */
public class StringTransformation {

    private static final Logger logger = LoggerFactory.getLogger(StringTransformation.class);

    private final ClassNode cn;

    /**
     * <p>
     * Constructor for StringTransformation.
     * </p>
     *
     * @param cn a {@link org.objectweb.asm.tree.ClassNode} object.
     */
    public StringTransformation(ClassNode cn) {
        this.cn = cn;
    }

    /**
     * <p>
     * transform
     * </p>
     *
     * @return a {@link org.objectweb.asm.tree.ClassNode} object.
     */
    @SuppressWarnings("unchecked")
    public ClassNode transform() {
        List<MethodNode> methodNodes = cn.methods;
        for (MethodNode mn : methodNodes) {
            if (transformMethod(mn)) {
                mn.maxStack++;
            }
        }

        return cn;
    }

    /**
     * Replace boolean-returning method calls on String classes
     *
     * @param mn
     */
    @SuppressWarnings("unchecked")
    private boolean transformStrings(MethodNode mn) {
        logger.info("Current method: " + mn.name);
        boolean changed = false;
        ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode node = iterator.next();
            if (node instanceof MethodInsnNode) {
                MethodInsnNode min = (MethodInsnNode) node;
                if (min.owner.equals("java/lang/String")) {
                    if (min.name.equals("equals")) {
                        changed = true;

                        MethodInsnNode equalCheck = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(StringHelper.class),
                                "StringEquals",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(String.class),
                                        Type.getType(Object.class)), false);
                        mn.instructions.insertBefore(node, equalCheck);
                        mn.instructions.remove(node);
						/*
												MethodInsnNode equalCheck = new MethodInsnNode(
												        Opcodes.INVOKESTATIC,
												        Type.getInternalName(BooleanHelper.class),
												        "StringEqualsCharacterDistance",
												        Type.getMethodDescriptor(Type.DOUBLE_TYPE,
												                                 new Type[] {
												                                         Type.getType(String.class),
												                                         Type.getType(Object.class) }));
												mn.instructions.insertBefore(node, equalCheck);
												mn.instructions.insertBefore(node, new LdcInsnNode(0.0));
												mn.instructions.insertBefore(node, new InsnNode(Opcodes.DCMPG));
												mn.instructions.remove(node);
												*/
                        TransformationStatistics.transformedStringComparison();

                    } else if (min.name.equals("equalsIgnoreCase")) {
                        changed = true;
                        MethodInsnNode equalCheck = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(StringHelper.class),
                                "StringEqualsIgnoreCase",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(String.class),
                                        Type.getType(String.class)), false);
                        mn.instructions.insertBefore(node, equalCheck);
                        mn.instructions.remove(node);
                        TransformationStatistics.transformedStringComparison();

                    } else if (min.name.equals("startsWith")) {
                        changed = true;
                        if (min.desc.equals("(Ljava/lang/String;)Z")) {
                            mn.instructions.insertBefore(node, new InsnNode(
                                    Opcodes.ICONST_0));
                        }
                        MethodInsnNode equalCheck = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(StringHelper.class),
                                "StringStartsWith",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(String.class),
                                        Type.getType(String.class),
                                        Type.INT_TYPE), false);
                        mn.instructions.insertBefore(node, equalCheck);
                        mn.instructions.remove(node);
                        TransformationStatistics.transformedStringComparison();

                    } else if (min.name.equals("endsWith")) {
                        changed = true;
                        MethodInsnNode equalCheck = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(StringHelper.class),
                                "StringEndsWith",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(String.class),
                                        Type.getType(String.class)), false);
                        mn.instructions.insertBefore(node, equalCheck);
                        mn.instructions.remove(node);
                        TransformationStatistics.transformedStringComparison();

                    } else if (min.name.equals("isEmpty")) {
                        changed = true;
                        MethodInsnNode equalCheck = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(StringHelper.class),
                                "StringIsEmpty",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(String.class)), false);
                        mn.instructions.insertBefore(node, equalCheck);
                        mn.instructions.remove(node);
                        TransformationStatistics.transformedStringComparison();
                    } else if (min.name.equals("matches")) {
                        changed = true;
                        MethodInsnNode equalCheck = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(StringHelper.class),
                                "StringMatches",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(String.class),
                                        Type.getType(String.class)), false);
                        mn.instructions.insertBefore(node, equalCheck);
                        mn.instructions.remove(node);
                        TransformationStatistics.transformedStringComparison();
                    } else if (min.name.equals("regionMatches")) {
                        Type[] argumentTypes = Type.getArgumentTypes(min.desc);
                        if (argumentTypes.length == 4) {
                            changed = true;
                            MethodInsnNode equalCheck = new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    Type.getInternalName(StringHelper.class),
                                    "StringRegionMatches",
                                    Type.getMethodDescriptor(Type.INT_TYPE, Type.getType(String.class), Type.INT_TYPE,
                                            Type.getType(String.class), Type.INT_TYPE,
                                            Type.INT_TYPE), false);
                            mn.instructions.insertBefore(node, equalCheck);
                            mn.instructions.remove(node);
                            TransformationStatistics.transformedStringComparison();

                        } else if (argumentTypes.length == 5) {
                            changed = true;
                            MethodInsnNode equalCheck = new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    Type.getInternalName(StringHelper.class),
                                    "StringRegionMatches",
                                    Type.getMethodDescriptor(Type.INT_TYPE,
                                            Type.getType(String.class),
                                            Type.BOOLEAN_TYPE,
                                            Type.INT_TYPE,
                                            Type.getType(String.class),
                                            Type.INT_TYPE,
                                            Type.INT_TYPE), false);
                            mn.instructions.insertBefore(node, equalCheck);
                            mn.instructions.remove(node);
                            TransformationStatistics.transformedStringComparison();
                        }
                    }

                } else if (min.owner.equals("java/util/regex/Pattern")) {
                    if (min.name.equals("matches")) {
                        changed = true;
                        MethodInsnNode equalCheck = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(StringHelper.class),
                                "StringMatchRegex",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(String.class),
                                        Type.getType(CharSequence.class)), false);
                        mn.instructions.insertBefore(node, equalCheck);
                        mn.instructions.remove(node);
                    }
                } else if (min.owner.equals("java/util/regex/Matcher")) {
                    if (min.name.equals("matches")) {
                        changed = true;
                        MethodInsnNode equalCheck = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(StringHelper.class),
                                "StringMatchRegex",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(Matcher.class)), false);
                        mn.instructions.insertBefore(node, equalCheck);
                        mn.instructions.remove(node);
                    }
                }
            }
        }
        return changed;
    }

    private static boolean isStringMethod(AbstractInsnNode node) {
        if (node.getOpcode() == Opcodes.INVOKESTATIC) {
            MethodInsnNode methodInsnNode = (MethodInsnNode) node;
            return methodInsnNode.owner.equals(Type.getInternalName(StringHelper.class))
                    && methodInsnNode.name.startsWith("String");
        }
        return false;
    }

    /**
     * <p>
     * transformMethod
     * </p>
     *
     * @param mn a {@link org.objectweb.asm.tree.MethodNode} object.
     * @return a boolean.
     */
    public boolean transformMethod(MethodNode mn) {
        boolean changed = transformStrings(mn);
        if (changed) {
            try {
                mn.maxStack++;
                Analyzer a = new Analyzer(new StringBooleanInterpreter());
                a.analyze(cn.name, mn);
                Frame[] frames = a.getFrames();
                AbstractInsnNode node = mn.instructions.getFirst();
                boolean done = false;
                while (!done) {
                    if (node == mn.instructions.getLast())
                        done = true;
                    AbstractInsnNode next = node.getNext();
                    int index = mn.instructions.indexOf(node);
                    if (index >= frames.length)
                        break;
                    Frame current = frames[index];
                    if (current == null)
                        break;
                    int size = current.getStackSize();
                    if (node.getOpcode() == Opcodes.IFNE) {
                        JumpInsnNode branch = (JumpInsnNode) node;
                        if (current.getStack(size - 1) == StringBooleanInterpreter.STRING_BOOLEAN
                                || isStringMethod(node.getPrevious())) {
                            logger.info("IFNE -> IFGT");
                            branch.setOpcode(Opcodes.IFGT);
                        }
                    } else if (node.getOpcode() == Opcodes.IFEQ) {
                        JumpInsnNode branch = (JumpInsnNode) node;
                        if (current.getStack(size - 1) == StringBooleanInterpreter.STRING_BOOLEAN
                                || isStringMethod(node.getPrevious())) {
                            logger.info("IFEQ -> IFLE");
                            branch.setOpcode(Opcodes.IFLE);
                        }
                    } else if (node.getOpcode() == Opcodes.IF_ICMPEQ) {
                        JumpInsnNode branch = (JumpInsnNode) node;
                        if (current.getStack(size - 2) == StringBooleanInterpreter.STRING_BOOLEAN
                                || isStringMethod(node.getPrevious().getPrevious())) {
                            if (node.getPrevious().getOpcode() == Opcodes.ICONST_0) {
                                branch.setOpcode(Opcodes.IFLE);
                                mn.instructions.remove(node.getPrevious());
                            } else if (node.getPrevious().getOpcode() == Opcodes.ICONST_1) {
                                branch.setOpcode(Opcodes.IFGT);
                                mn.instructions.remove(node.getPrevious());
                            }
                        }
                    } else if (node.getOpcode() == Opcodes.IF_ICMPNE) {
                        JumpInsnNode branch = (JumpInsnNode) node;
                        if (current.getStack(size - 2) == StringBooleanInterpreter.STRING_BOOLEAN
                                || isStringMethod(node.getPrevious().getPrevious())) {
                            if (node.getPrevious().getOpcode() == Opcodes.ICONST_0) {
                                branch.setOpcode(Opcodes.IFGT);
                                mn.instructions.remove(node.getPrevious());
                            } else if (node.getPrevious().getOpcode() == Opcodes.ICONST_1) {
                                branch.setOpcode(Opcodes.IFLE);
                                mn.instructions.remove(node.getPrevious());
                            }
                        }
                    } else if (node.getOpcode() == Opcodes.IRETURN) {
                        if (current.getStack(size - 1) == StringBooleanInterpreter.STRING_BOOLEAN
                                || isStringMethod(node.getPrevious())) {
                            logger.info("IFEQ -> IFLE");
                            MethodInsnNode n = new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    Type.getInternalName(BooleanHelper.class),
                                    "intToBoolean",
                                    Type.getMethodDescriptor(Type.BOOLEAN_TYPE,
                                            Type.INT_TYPE), false);

                            mn.instructions.insertBefore(node, n);
                        }
                    }
                    node = next;
                }
            } catch (Exception e) {
                logger.warn("EXCEPTION DURING STRING TRANSFORMATION: " + e);
                return changed;
            }
        }
        return changed;
    }
}
