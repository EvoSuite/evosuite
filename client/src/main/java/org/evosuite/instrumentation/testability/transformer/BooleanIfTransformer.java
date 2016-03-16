/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
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
package org.evosuite.instrumentation.testability.transformer;

import org.evosuite.instrumentation.testability.BooleanHelper;
import org.evosuite.instrumentation.testability.BooleanTestabilityTransformation;
import org.evosuite.instrumentation.testability.DescriptorMapping;
import org.evosuite.instrumentation.TransformationStatistics;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;

/**
 * Transform IFEQ/IFNE to IFLE/IFGT for transformed Boolean variables
 */
public class BooleanIfTransformer extends MethodNodeTransformer {

	/**
	 * 
	 */
	private final BooleanTestabilityTransformation booleanTestabilityTransformation;

	/**
	 * @param booleanTestabilityTransformation
	 */
	public BooleanIfTransformer(
			BooleanTestabilityTransformation booleanTestabilityTransformation) {
		this.booleanTestabilityTransformation = booleanTestabilityTransformation;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.instrumentation.MethodNodeTransformer#transformJumpInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.JumpInsnNode)
	 */
	@Override
	protected AbstractInsnNode transformJumpInsnNode(MethodNode mn,
	        JumpInsnNode jumpNode) {
		if (jumpNode.getOpcode() == Opcodes.IFNE) {
			if (this.booleanTestabilityTransformation.isBooleanOnStack(mn, jumpNode, 0)) {
				TransformationStatistics.transformedBooleanComparison();
				BooleanTestabilityTransformation.logger.info("Changing IFNE");
				jumpNode.setOpcode(Opcodes.IFGT);
			} else {
				BooleanTestabilityTransformation.logger.info("Not changing IFNE");
				int insnPosition = mn.instructions.indexOf(jumpNode);
				Frame frame = this.booleanTestabilityTransformation.currentFrames[insnPosition];
				AbstractInsnNode insn = mn.instructions.get(insnPosition - 1);
				BooleanTestabilityTransformation.logger.info("Current node: " + mn.instructions.get(insnPosition));
				BooleanTestabilityTransformation.logger.info("Previous node: " + insn);
				if (insn instanceof MethodInsnNode) {
					MethodInsnNode mi = (MethodInsnNode) insn;
					if (Type.getReturnType(DescriptorMapping.getInstance().getMethodDesc(mi.owner,
					                                                                     mi.name,
					                                                                     mi.desc)) == Type.BOOLEAN_TYPE) {
						BooleanTestabilityTransformation.logger.info("Changing IFNE");
						jumpNode.setOpcode(Opcodes.IFGT);
					}
					BooleanTestabilityTransformation.logger.info("Method: " + mi.name);
				}
				BooleanTestabilityTransformation.logger.info("Stack size: " + frame.getStackSize());

				//logger.info("Top of stack: " + frame.getStack(0));
				for (int i = 0; i < frame.getStackSize(); i++) {
					BooleanTestabilityTransformation.logger.info(i + " Stack: " + frame.getStack(i));
				}
			}
		} else if (jumpNode.getOpcode() == Opcodes.IFEQ) {
			if (this.booleanTestabilityTransformation.isBooleanOnStack(mn, jumpNode, 0)) {
				TransformationStatistics.transformedBooleanComparison();
				BooleanTestabilityTransformation.logger.info("Changing IFEQ");
				jumpNode.setOpcode(Opcodes.IFLE);
			} else {
				BooleanTestabilityTransformation.logger.info("Not changing IFEQ");
				int insnPosition = mn.instructions.indexOf(jumpNode);
				Frame frame = this.booleanTestabilityTransformation.currentFrames[insnPosition];
				AbstractInsnNode insn = mn.instructions.get(insnPosition - 1);
				BooleanTestabilityTransformation.logger.info("Previous node: " + insn);
				if (insn instanceof MethodInsnNode) {
					MethodInsnNode mi = (MethodInsnNode) insn;
					BooleanTestabilityTransformation.logger.info("Method: " + mi.name);
					if (Type.getReturnType(BooleanTestabilityTransformation.getOriginalDesc(mi.owner, mi.name, mi.desc)) == Type.BOOLEAN_TYPE) {
						BooleanTestabilityTransformation.logger.info("Changing IFEQ");
						jumpNode.setOpcode(Opcodes.IFLE);
					} else {
						BooleanTestabilityTransformation.logger.info("Return type: "
						        + Type.getReturnType(BooleanTestabilityTransformation.getOriginalDesc(mi.owner,
						                                             mi.name, mi.desc)));
					}

				}
				BooleanTestabilityTransformation.logger.info("Stack size: " + frame.getStackSize());
				for (int i = 0; i < frame.getStackSize(); i++) {
					BooleanTestabilityTransformation.logger.info(i + " Stack: " + frame.getStack(i));
				}
			}
		} else if (jumpNode.getOpcode() == Opcodes.IF_ICMPEQ) {
			if (this.booleanTestabilityTransformation.isBooleanOnStack(mn, jumpNode, 0)) {
				InsnList convert = new InsnList();
				convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class), "pushParameter",
				        Type.getMethodDescriptor(Type.VOID_TYPE,
				                                 new Type[] { Type.INT_TYPE })));
				convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class), "pushParameter",
				        Type.getMethodDescriptor(Type.VOID_TYPE,
				                                 new Type[] { Type.INT_TYPE })));
				convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class),
				        "popParameterBooleanFromInt",
				        Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] {})));
				convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class),
				        "popParameterBooleanFromInt",
				        Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] {})));
				mn.instructions.insertBefore(jumpNode, convert);
				TransformationStatistics.transformedBooleanComparison();
			}
		} else if (jumpNode.getOpcode() == Opcodes.IF_ICMPNE) {
			if (this.booleanTestabilityTransformation.isBooleanOnStack(mn, jumpNode, 0)) {
				InsnList convert = new InsnList();
				convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class), "pushParameter",
				        Type.getMethodDescriptor(Type.VOID_TYPE,
				                                 new Type[] { Type.INT_TYPE })));
				convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class), "pushParameter",
				        Type.getMethodDescriptor(Type.VOID_TYPE,
				                                 new Type[] { Type.INT_TYPE })));
				convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class),
				        "popParameterBooleanFromInt",
				        Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] {})));
				convert.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
				        Type.getInternalName(BooleanHelper.class),
				        "popParameterBooleanFromInt",
				        Type.getMethodDescriptor(Type.BOOLEAN_TYPE, new Type[] {})));
				mn.instructions.insertBefore(jumpNode, convert);
				TransformationStatistics.transformedBooleanComparison();
			}
		}
		return jumpNode;
	}
}