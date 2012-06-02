/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fraser
 * 
 */
public class ContainerTransformation {

	private static Logger logger = LoggerFactory.getLogger(ContainerTransformation.class);

	ClassNode cn;

	public ContainerTransformation(ClassNode cn) {
		this.cn = cn;
	}

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
	private boolean transformContainers(MethodNode mn) {
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
						MethodInsnNode n = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "collectionIsEmpty",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] { Type.getType(Collection.class) }));
						mn.instructions.insertBefore(node, n);
						mn.instructions.remove(node);
						TransformationStatistics.transformedContainerComparison();

						changed = true;
					} else if (methodNode.name.equals("contains")) {
						MethodInsnNode n = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "collectionContains",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(Collection.class),
						                                         Type.getType(Object.class) }));
						mn.instructions.insertBefore(node, n);
						mn.instructions.remove(node);
						TransformationStatistics.transformedContainerComparison();

						changed = true;
					} else if (methodNode.name.equals("containsAll")) {
						MethodInsnNode n = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "collectionContainsAll",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(Collection.class),
						                                         Type.getType(Collection.class) }));
						mn.instructions.insertBefore(node, n);
						mn.instructions.remove(node);
						TransformationStatistics.transformedContainerComparison();
						changed = true;
					}
				} else if (methodNode.owner.equals("java/util/Map")) {
					if (methodNode.name.equals("isEmpty")) {
						MethodInsnNode n = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "mapIsEmpty",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] { Type.getType(Map.class) }));
						mn.instructions.insertBefore(node, n);
						mn.instructions.remove(node);
						TransformationStatistics.transformedContainerComparison();
						changed = true;
					} else if (methodNode.name.equals("containsKey")) {
						MethodInsnNode n = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "mapContainsKey",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(Map.class),
						                                         Type.getType(Object.class) }));
						mn.instructions.insertBefore(node, n);
						mn.instructions.remove(node);
						TransformationStatistics.transformedContainerComparison();
						changed = true;
					} else if (methodNode.name.equals("containsValue")) {
						MethodInsnNode n = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "mapContainsValue",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(Map.class),
						                                         Type.getType(Object.class) }));
						mn.instructions.insertBefore(node, n);
						mn.instructions.remove(node);
						TransformationStatistics.transformedContainerComparison();
						changed = true;
					}
				}
			}
		}
		return changed;
	}

	public boolean transformMethod(MethodNode mn) {

		boolean changed = transformContainers(mn);
		if (changed) {
			try {
				Analyzer a = new Analyzer(new ContainerBooleanInterpreter());
				a.analyze(cn.name, mn);
				Frame[] frames = a.getFrames();
				AbstractInsnNode node = mn.instructions.getFirst();
				while (node != mn.instructions.getLast()) {
					AbstractInsnNode next = node.getNext();
					Frame current = frames[mn.instructions.indexOf(node)];
					int size = current.getStackSize();
					if (node.getOpcode() == Opcodes.IFNE) {
						JumpInsnNode branch = (JumpInsnNode) node;
						if (current.getStack(size - 1) == ContainerBooleanInterpreter.CONTAINER_BOOLEAN) {
							logger.info("IFNE -> IFGT");
							branch.setOpcode(Opcodes.IFGT);
						}
					} else if (node.getOpcode() == Opcodes.IFEQ) {
						JumpInsnNode branch = (JumpInsnNode) node;
						if (current.getStack(size - 1) == ContainerBooleanInterpreter.CONTAINER_BOOLEAN) {
							logger.info("IFEQ -> IFLE");
							branch.setOpcode(Opcodes.IFLE);
						}
					}
					node = next;
				}
			} catch (Exception e) {

				return changed;
			}
		}
		return changed;
	}

}
