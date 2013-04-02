package org.evosuite.instrumentation.testability;

import org.evosuite.instrumentation.BooleanTestabilityTransformation;
import org.evosuite.instrumentation.TransformationStatistics;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * This transformer inserts calls to the put function before a Boolean
 * predicate
 */
public class BooleanDistanceTransformer extends MethodNodeTransformer {
	/**
	 * 
	 */
	private final BooleanTestabilityTransformation booleanTestabilityTransformation;

	/**
	 * @param booleanTestabilityTransformation
	 */
	public BooleanDistanceTransformer(
			BooleanTestabilityTransformation booleanTestabilityTransformation) {
		this.booleanTestabilityTransformation = booleanTestabilityTransformation;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.instrumentation.MethodNodeTransformer#transformJumpInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.JumpInsnNode)
	 */
	@Override
	protected AbstractInsnNode transformJumpInsnNode(MethodNode mn,
	        JumpInsnNode jumpNode) {

		switch (jumpNode.getOpcode()) {
		case Opcodes.IFEQ:
		case Opcodes.IFNE:
		case Opcodes.IFLT:
		case Opcodes.IFGE:
		case Opcodes.IFGT:
		case Opcodes.IFLE:
			TransformationStatistics.insertPush(jumpNode.getOpcode());
			this.booleanTestabilityTransformation.insertPush(jumpNode.getOpcode(), jumpNode, mn.instructions);
			break;
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPGT:
		case Opcodes.IF_ICMPLE:
			TransformationStatistics.insertPush(jumpNode.getOpcode());
			this.booleanTestabilityTransformation.insertPush2(jumpNode.getOpcode(), jumpNode, mn.instructions);
			break;
		case Opcodes.IFNULL:
		case Opcodes.IFNONNULL:
			TransformationStatistics.insertPush(jumpNode.getOpcode());
			this.booleanTestabilityTransformation.insertPushNull(jumpNode.getOpcode(), jumpNode, mn.instructions);
			break;
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ACMPNE:
			TransformationStatistics.insertPush(jumpNode.getOpcode());
			this.booleanTestabilityTransformation.insertPushEquals(jumpNode.getOpcode(), jumpNode, mn.instructions);
			break;
		default:
			// GOTO, JSR: Do nothing
		}
		return jumpNode;
	}
}