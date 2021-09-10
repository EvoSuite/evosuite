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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * <p>ContainerTransformation class.</p>
 *
 * @author fraser
 */
public class ContainerTransformation {

    private static final Logger logger = LoggerFactory
            .getLogger(ContainerTransformation.class);

    private final ClassNode cn;

    /**
     * <p>Constructor for ContainerTransformation.</p>
     *
     * @param cn a {@link org.objectweb.asm.tree.ClassNode} object.
     */
    public ContainerTransformation(ClassNode cn) {
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
            if (transformMethod(mn)) {
                mn.maxStack++;
            }
        }

        return cn;
    }

    /**
     * Replace boolean-returning method calls on Collection classes
     *
     * @param mn
     */
    @SuppressWarnings("unchecked")
    public boolean transformMethod(MethodNode mn) {
        boolean changed = false;
        ListIterator<AbstractInsnNode> iterator = mn.instructions.iterator();
        while (iterator.hasNext()) {
            AbstractInsnNode node = iterator.next();

            if (node instanceof MethodInsnNode) {
                MethodInsnNode methodNode = (MethodInsnNode) node;
                if (methodNode.owner.equals("java/util/Collection")
                        || methodNode.owner.equals("java/util/List")
                        || methodNode.owner.equals("java/util/ArrayList")
                        || methodNode.owner.equals("java/util/Set")
                        || methodNode.owner.equals("java/util/Queue")
                        || methodNode.owner.equals("java/util/SortedSet")) {
                    if (methodNode.name.equals("isEmpty")) {

                        logger.debug("Test Transformation of " + methodNode.owner + "." + methodNode.name
                                + " -> " + Type.getInternalName(ContainerHelper.class) + "." + "collectionIsEmpty");

                        MethodInsnNode n = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(ContainerHelper.class),
                                "collectionIsEmpty",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(Collection.class)), false);

                        InsnList il = createNewIfThenElse(n);
                        mn.instructions.insertBefore(node, il);
                        mn.instructions.remove(node);
                        TransformationStatistics.transformedContainerComparison();

                        changed = true;
                    } else if (methodNode.name.equals("contains")) {

                        logger.debug("Test Transformation of " + methodNode.owner + "." + methodNode.name
                                + " -> " + Type.getInternalName(ContainerHelper.class) + "." + "collectionContains");

                        MethodInsnNode n = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(ContainerHelper.class),
                                "collectionContains",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(Collection.class),
                                        Type.getType(Object.class)), false);
                        InsnList il = createNewIfThenElse(n);
                        mn.instructions.insertBefore(node, il);
                        mn.instructions.remove(node);
                        TransformationStatistics.transformedContainerComparison();

                        changed = true;
                    } else if (methodNode.name.equals("containsAll")) {

                        logger.debug("Test Transformation of " + methodNode.owner + "." + methodNode.name
                                + " -> " + Type.getInternalName(ContainerHelper.class) + "." + "collectionContainsAll");

                        MethodInsnNode n = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(ContainerHelper.class),
                                "collectionContainsAll",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(Collection.class),
                                        Type.getType(Collection.class)), false);
                        InsnList il = createNewIfThenElse(n);
                        mn.instructions.insertBefore(node, il);
                        mn.instructions.remove(node);
                        TransformationStatistics.transformedContainerComparison();
                        changed = true;
                    }
                } else if (methodNode.owner.equals("java/util/Map")) {
                    if (methodNode.name.equals("isEmpty")) {

                        logger.debug("Test Transformation of " + methodNode.owner + "." + methodNode.name
                                + " -> " + Type.getInternalName(ContainerHelper.class) + "." + "mapIsEmpty");

                        MethodInsnNode n = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(ContainerHelper.class),
                                "mapIsEmpty",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(Map.class)), false);
                        InsnList il = createNewIfThenElse(n);
                        mn.instructions.insertBefore(node, il);
                        mn.instructions.remove(node);
                        TransformationStatistics.transformedContainerComparison();
                        changed = true;
                    } else if (methodNode.name.equals("containsKey")) {

                        logger.debug("Test Transformation of " + methodNode.owner + "." + methodNode.name
                                + " -> " + Type.getInternalName(ContainerHelper.class) + "." + "mapContainsKey");

                        MethodInsnNode n = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(ContainerHelper.class),
                                "mapContainsKey",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(Map.class),
                                        Type.getType(Object.class)), false);
                        InsnList il = createNewIfThenElse(n);
                        mn.instructions.insertBefore(node, il);
                        mn.instructions.remove(node);
                        TransformationStatistics.transformedContainerComparison();
                        changed = true;
                    } else if (methodNode.name.equals("containsValue")) {

                        logger.debug("Test Transformation of " + methodNode.owner + "." + methodNode.name
                                + " -> " + Type.getInternalName(ContainerHelper.class) + "." + "mapContainsValue");

                        MethodInsnNode n = new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                Type.getInternalName(ContainerHelper.class),
                                "mapContainsValue",
                                Type.getMethodDescriptor(Type.INT_TYPE,
                                        Type.getType(Map.class),
                                        Type.getType(Object.class)), false);
                        InsnList il = createNewIfThenElse(n);
                        mn.instructions.insertBefore(node, il);
                        mn.instructions.remove(node);
                        TransformationStatistics.transformedContainerComparison();
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private static InsnList createNewIfThenElse(MethodInsnNode n) {
        LabelNode labelIsNotEmpty = new LabelNode();
        LabelNode labelEndif = new LabelNode();
        InsnList il = new InsnList();
        il.add(n);
        il.add(new JumpInsnNode(Opcodes.IFLE, labelIsNotEmpty));
        il.add(new InsnNode(Opcodes.ICONST_1));
        il.add(new JumpInsnNode(Opcodes.GOTO, labelEndif));
        il.add(labelIsNotEmpty);
        il.add(new InsnNode(Opcodes.ICONST_0));
        il.add(labelEndif);
        return il;
    }

}
