package org.evosuite.instrumentation.testability;

import org.evosuite.instrumentation.BooleanHelper;
import org.evosuite.instrumentation.BooleanTestabilityTransformation;
import org.evosuite.instrumentation.TransformationStatistics;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Make sure bitwise operations on transformed Booleans are still valid
 */
public class BitwiseOperatorTransformer extends MethodNodeTransformer {
	/**
	 * 
	 */
	private final BooleanTestabilityTransformation booleanTestabilityTransformation;

	/**
	 * @param booleanTestabilityTransformation
	 */
	public BitwiseOperatorTransformer(
			BooleanTestabilityTransformation booleanTestabilityTransformation) {
		this.booleanTestabilityTransformation = booleanTestabilityTransformation;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.instrumentation.MethodNodeTransformer#transformInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.InsnNode)
	 */
	@Override
	protected AbstractInsnNode transformInsnNode(MethodNode mn, InsnNode insnNode) {
		if (insnNode.getOpcode() == Opcodes.IOR
		        || insnNode.getOpcode() == Opcodes.IAND
		        || insnNode.getOpcode() == Opcodes.IXOR) {

			if (this.booleanTestabilityTransformation.isBooleanOnStack(mn, insnNode, 0)
			        && this.booleanTestabilityTransformation.isBooleanOnStack(mn, insnNode, 1)) {
				if (insnNode.getOpcode() == Opcodes.IOR) {
					MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "IOR",
					        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
					                Type.INT_TYPE, Type.INT_TYPE }));
					mn.instructions.insertBefore(insnNode, push);
					mn.instructions.remove(insnNode);
					TransformationStatistics.transformedBitwise();
					return push;
				} else if (insnNode.getOpcode() == Opcodes.IAND) {
					MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "IAND",
					        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
					                Type.INT_TYPE, Type.INT_TYPE }));
					mn.instructions.insertBefore(insnNode, push);
					mn.instructions.remove(insnNode);
					TransformationStatistics.transformedBitwise();
					return push;

				} else if (insnNode.getOpcode() == Opcodes.IXOR) {
					MethodInsnNode push = new MethodInsnNode(Opcodes.INVOKESTATIC,
					        Type.getInternalName(BooleanHelper.class), "IXOR",
					        Type.getMethodDescriptor(Type.INT_TYPE, new Type[] {
					                Type.INT_TYPE, Type.INT_TYPE }));
					mn.instructions.insertBefore(insnNode, push);
					mn.instructions.remove(insnNode);
					TransformationStatistics.transformedBitwise();
					return push;
				}
			}
		}
		return insnNode;
	}
}