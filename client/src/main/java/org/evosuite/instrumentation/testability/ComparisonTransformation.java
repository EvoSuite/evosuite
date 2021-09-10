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

import java.util.List;

/**
 * <p>ComparisonTransformation class.</p>
 *
 * @author Gordon Fraser
 */
public class ComparisonTransformation {

    private final ClassNode cn;

    /**
     * <p>Constructor for ComparisonTransformation.</p>
     *
     * @param cn a {@link org.objectweb.asm.tree.ClassNode} object.
     */
    public ComparisonTransformation(ClassNode cn) {
        this.cn = cn;
    }

    /**
     * <p>transform</p>
     *
     * @return a {@link org.objectweb.asm.tree.ClassNode} object.
     */
    @SuppressWarnings("unchecked")
    public ClassNode transform() {
        List<MethodNode> methodNodes = cn.methods;
        for (MethodNode mn : methodNodes) {
            transformMethod(mn);
        }
        return cn;
    }

    /**
     * <p>transformMethod</p>
     *
     * @param mn a {@link org.objectweb.asm.tree.MethodNode} object.
     */
    public void transformMethod(MethodNode mn) {
        AbstractInsnNode node = mn.instructions.getFirst();
        while (node != mn.instructions.getLast()) {
            AbstractInsnNode next = node.getNext();
            if (node instanceof InsnNode) {
                InsnNode in = (InsnNode) node;
                if (in.getOpcode() == Opcodes.LCMP) {
                    insertLongComparison(in, mn.instructions);
                    TransformationStatistics.transformedComparison();
                } else if (in.getOpcode() == Opcodes.DCMPG) {
                    TransformationStatistics.transformedComparison();
                    insertDoubleComparisonG(in, mn.instructions);
                } else if (in.getOpcode() == Opcodes.DCMPL) {
                    TransformationStatistics.transformedComparison();
                    insertDoubleComparisonL(in, mn.instructions);
                } else if (in.getOpcode() == Opcodes.FCMPG) {
                    TransformationStatistics.transformedComparison();
                    insertFloatComparisonG(in, mn.instructions);
                } else if (in.getOpcode() == Opcodes.FCMPL) {
                    TransformationStatistics.transformedComparison();
                    insertFloatComparisonL(in, mn.instructions);
                }
            }
            node = next;
        }
    }

    private void insertLongComparison(AbstractInsnNode position, InsnList list) {
        MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "longSub",
                Type.getMethodDescriptor(Type.INT_TYPE, Type.LONG_TYPE,
                        Type.LONG_TYPE), false);
        list.insert(position, get);
        list.remove(position);
    }

    private void insertFloatComparisonG(AbstractInsnNode position, InsnList list) {
        MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "floatSubG",
                Type.getMethodDescriptor(Type.INT_TYPE, Type.FLOAT_TYPE,
                        Type.FLOAT_TYPE), false);
        list.insert(position, get);
        list.remove(position);
    }

    private void insertFloatComparisonL(AbstractInsnNode position, InsnList list) {
        MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "floatSubL",
                Type.getMethodDescriptor(Type.INT_TYPE, Type.FLOAT_TYPE,
                        Type.FLOAT_TYPE), false);
        list.insert(position, get);
        list.remove(position);
    }

    private void insertDoubleComparisonG(AbstractInsnNode position, InsnList list) {
        MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "doubleSubG",
                Type.getMethodDescriptor(Type.INT_TYPE, Type.DOUBLE_TYPE,
                        Type.DOUBLE_TYPE), false);
        list.insert(position, get);
        list.remove(position);
    }

    private void insertDoubleComparisonL(AbstractInsnNode position, InsnList list) {
        MethodInsnNode get = new MethodInsnNode(Opcodes.INVOKESTATIC,
                Type.getInternalName(BooleanHelper.class), "doubleSubL",
                Type.getMethodDescriptor(Type.INT_TYPE, Type.DOUBLE_TYPE,
                        Type.DOUBLE_TYPE), false);
        list.insert(position, get);
        list.remove(position);
    }

}
