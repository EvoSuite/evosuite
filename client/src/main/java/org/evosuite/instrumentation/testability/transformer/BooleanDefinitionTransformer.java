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

import org.evosuite.instrumentation.testability.BooleanTestabilityTransformation;
import org.evosuite.instrumentation.testability.DescriptorMapping;
import org.evosuite.instrumentation.TransformationStatistics;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * This transformer inserts calls to the get function when a Boolean is put
 * on the stack
 */
public class BooleanDefinitionTransformer extends MethodNodeTransformer {

	// Get branch id
	// Get last distance for this branch id, else +/-K

	/**
	 * 
	 */
	private final BooleanTestabilityTransformation booleanTestabilityTransformation;

	/**
	 * @param booleanTestabilityTransformation
	 */
	public BooleanDefinitionTransformer(
			BooleanTestabilityTransformation booleanTestabilityTransformation) {
		this.booleanTestabilityTransformation = booleanTestabilityTransformation;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.instrumentation.MethodNodeTransformer#transformInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.InsnNode)
	 */
	@Override
	protected AbstractInsnNode transformInsnNode(MethodNode mn, InsnNode insnNode) {
		BooleanTestabilityTransformation.logger.info("Checking transformation of InsnNode ");
		if (insnNode.getOpcode() == Opcodes.ICONST_0
		        && this.booleanTestabilityTransformation.isBooleanAssignment(insnNode, mn)) {
			TransformationStatistics.insertedGet();
			this.booleanTestabilityTransformation.insertGet(insnNode, mn.instructions);
		} else if (insnNode.getOpcode() == Opcodes.ICONST_1
		        && this.booleanTestabilityTransformation.isBooleanAssignment(insnNode, mn)) {
			TransformationStatistics.insertedGet();
			this.booleanTestabilityTransformation.insertGet(insnNode, mn.instructions);
			//} else if (insnNode.getOpcode() == Opcodes.IRETURN
			//        && isBooleanAssignment(insnNode, mn)) {
			//	TransformationStatistics.insertedGet();
			//	insertGetBefore(insnNode, mn.instructions);
		}
		return insnNode;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.instrumentation.MethodNodeTransformer#transformVarInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.VarInsnNode)
	 */
	@Override
	protected AbstractInsnNode transformVarInsnNode(MethodNode mn, VarInsnNode varNode) {
		// Special case for implicit else branch
		if (this.booleanTestabilityTransformation.isBooleanVariable(varNode.var, mn)
		        && varNode.getNext() instanceof VarInsnNode) {
			VarInsnNode vn2 = (VarInsnNode) varNode.getNext();
			if (varNode.var == vn2.var) {
				this.booleanTestabilityTransformation.insertGet(varNode, mn.instructions);
			}
		}
		return varNode;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.instrumentation.MethodNodeTransformer#transformFieldInsnNode(org.objectweb.asm.tree.MethodNode, org.objectweb.asm.tree.FieldInsnNode)
	 */
	@Override
	protected AbstractInsnNode transformFieldInsnNode(MethodNode mn,
	        FieldInsnNode fieldNode) {
		// This handles the else branch for field assignments
		if (DescriptorMapping.getInstance().isTransformedOrBooleanField(this.booleanTestabilityTransformation.className,
		                                                                fieldNode.name,
		                                                                fieldNode.desc)) {
			if (fieldNode.getNext() instanceof FieldInsnNode) {
				FieldInsnNode other = (FieldInsnNode) fieldNode.getNext();
				if (fieldNode.owner.equals(other.owner)
				        && fieldNode.name.equals(other.name)
				        && fieldNode.desc.equals(other.desc)) {
					if (fieldNode.getOpcode() == Opcodes.GETFIELD
					        && other.getOpcode() == Opcodes.PUTFIELD) {
						this.booleanTestabilityTransformation.insertGetBefore(other, mn.instructions);
					} else if (fieldNode.getOpcode() == Opcodes.GETSTATIC
					        && other.getOpcode() == Opcodes.PUTSTATIC) {
						this.booleanTestabilityTransformation.insertGetBefore(other, mn.instructions);
					}
				}
			}
		}
		return fieldNode;
	}

}