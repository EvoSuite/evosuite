/**
 * 
 */
package de.unisb.cs.st.evosuite.javaagent;

import java.util.List;
import java.util.ListIterator;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
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
public class StringTransformation {

	private static Logger logger = LoggerFactory.getLogger(StringTransformation.class);

	ClassNode cn;

	public StringTransformation(ClassNode cn) {
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
						        Type.getInternalName(BooleanHelper.class),
						        "StringEquals",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(String.class),
						                                         Type.getType(Object.class) }));
						mn.instructions.insertBefore(node, equalCheck);
						mn.instructions.remove(node);
						TransformationStatistics.transformedStringComparison();

					} else if (min.name.equals("equalsIgnoreCase")) {
						changed = true;
						MethodInsnNode equalCheck = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "StringEqualsIgnoreCase",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(String.class),
						                                         Type.getType(String.class) }));
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
						        Type.getInternalName(BooleanHelper.class),
						        "StringStartsWith",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(String.class),
						                                         Type.getType(String.class),
						                                         Type.INT_TYPE }));
						mn.instructions.insertBefore(node, equalCheck);
						mn.instructions.remove(node);
						TransformationStatistics.transformedStringComparison();

					} else if (min.name.equals("endsWith")) {
						changed = true;
						MethodInsnNode equalCheck = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "StringEndsWith",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] {
						                                         Type.getType(String.class),
						                                         Type.getType(String.class) }));
						mn.instructions.insertBefore(node, equalCheck);
						mn.instructions.remove(node);
						TransformationStatistics.transformedStringComparison();

					} else if (min.name.equals("isEmpty")) {
						changed = true;
						MethodInsnNode equalCheck = new MethodInsnNode(
						        Opcodes.INVOKESTATIC,
						        Type.getInternalName(BooleanHelper.class),
						        "StringIsEmpty",
						        Type.getMethodDescriptor(Type.INT_TYPE,
						                                 new Type[] { Type.getType(String.class) }));
						mn.instructions.insertBefore(node, equalCheck);
						mn.instructions.remove(node);
						TransformationStatistics.transformedStringComparison();

					} else if (min.name.equals("regionMatches")) {
						// TODO
					}

				}
			}
		}
		return changed;
	}

	private static boolean isStringMethod(AbstractInsnNode node) {
		if (node.getOpcode() == Opcodes.INVOKESTATIC) {
			MethodInsnNode methodInsnNode = (MethodInsnNode) node;
			return methodInsnNode.owner.equals(Type.getInternalName(BooleanHelper.class))
			        && methodInsnNode.name.startsWith("String");
		}
		return false;
	}

	public boolean transformMethod(MethodNode mn) {
		boolean changed = transformStrings(mn);
		if (changed) {
			try {
				Analyzer a = new Analyzer(new StringBooleanInterpreter());
				a.analyze(cn.name, mn);
				Frame[] frames = a.getFrames();
				AbstractInsnNode node = mn.instructions.getFirst();
				boolean done = false;
				while (!done) {
					if (node == mn.instructions.getLast())
						done = true;
					AbstractInsnNode next = node.getNext();
					Frame current = frames[mn.instructions.indexOf(node)];
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
							                                 new Type[] { Type.INT_TYPE }));

							mn.instructions.insertBefore(node, n);
						}
					}
					node = next;
				}
			} catch (Exception e) {
				logger.warn("EXCEPTION DURING STRING TRANSFORMATION: " + e);
				e.printStackTrace();
				return changed;
			}
		}
		return changed;
	}
}
