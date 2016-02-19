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
/**
 * 
 */
package org.evosuite.instrumentation.mutation;

import java.util.LinkedList;
import java.util.List;

import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * DeleteField class.
 * </p>
 * 
 * @author fraser
 */
public class DeleteField implements MutationOperator {

	private static Logger logger = LoggerFactory.getLogger(DeleteField.class);

	public static final String NAME = "DeleteField";
	
	/* (non-Javadoc)
	 * @see org.evosuite.cfg.instrumentation.mutation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, org.evosuite.cfg.BytecodeInstruction)
	 */
	/** {@inheritDoc} */
	@Override
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction, Frame frame) {
		List<Mutation> mutations = new LinkedList<Mutation>();

		FieldInsnNode node = (FieldInsnNode) instruction.getASMNode();
		Type fieldType = Type.getType(node.desc);

		// insert mutation into bytecode with conditional
		InsnList mutation = new InsnList();
		logger.debug("Mutation deletefield for statement " + node.name + node.desc);
		if (node.getOpcode() == Opcodes.GETFIELD) {
			logger.debug("Deleting source of type " + node.owner);
			mutation.add(new InsnNode(Opcodes.POP));
		}
		mutation.add(getDefault(fieldType));
		// insert mutation into pool
		Mutation mutationObject = MutationPool.addMutation(className,
		                                                   methodName,
		                                                   NAME + " " + node.name
		                                                           + node.desc,
		                                                   instruction,
		                                                   mutation,
		                                                   getInfectionDistance(node,
		                                                                        mutation));

		mutations.add(mutationObject);
		return mutations;
	}

	private static AbstractInsnNode getDefault(Type type) {
		if (type.equals(Type.BOOLEAN_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.INT_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.BYTE_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.CHAR_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.DOUBLE_TYPE)) {
			return new LdcInsnNode(0.0);
		} else if (type.equals(Type.FLOAT_TYPE)) {
			return new LdcInsnNode(0.0F);
		} else if (type.equals(Type.INT_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.LONG_TYPE)) {
			return new LdcInsnNode(0L);
		} else if (type.equals(Type.SHORT_TYPE)) {
			return new LdcInsnNode(0);
		} else if (type.equals(Type.VOID_TYPE)) {
			return new LabelNode();
		} else {
			return new InsnNode(Opcodes.ACONST_NULL);
		}
	}

	/**
	 * <p>
	 * getInfectionDistance
	 * </p>
	 * 
	 * @param original
	 *            a {@link org.objectweb.asm.tree.FieldInsnNode} object.
	 * @param mutant
	 *            a {@link org.objectweb.asm.tree.InsnList} object.
	 * @return a {@link org.objectweb.asm.tree.InsnList} object.
	 */
	public InsnList getInfectionDistance(FieldInsnNode original, InsnList mutant) {
		InsnList distance = new InsnList();

		if (original.getOpcode() == Opcodes.GETFIELD)
			distance.add(new InsnNode(Opcodes.DUP)); //make sure to re-load this for GETFIELD

		distance.add(new FieldInsnNode(original.getOpcode(), original.owner,
		        original.name, original.desc));
		Type type = Type.getType(original.desc);

		if (type.getDescriptor().startsWith("L") || type.getDescriptor().startsWith("[")) {
			ReplaceVariable.addReferenceDistanceCheck(distance, type, mutant);
		} else {
			ReplaceVariable.addPrimitiveDistanceCheck(distance, type, mutant);
		}

		return distance;
	}

	/**
	 * <p>
	 * getDistance
	 * </p>
	 * 
	 * @param val1
	 *            a double.
	 * @param val2
	 *            a double.
	 * @return a double.
	 */
	public static double getDistance(double val1, double val2) {
		return val1 == val2 ? 1.0 : 0.0;
	}

	/**
	 * <p>
	 * getDistance
	 * </p>
	 * 
	 * @param obj1
	 *            a {@link java.lang.Object} object.
	 * @param obj2
	 *            a {@link java.lang.Object} object.
	 * @return a double.
	 */
	public static double getDistance(Object obj1, Object obj2) {
		if (obj1 == null) {
			return obj2 == null ? 1.0 : 0.0;
		} else {
			return obj1.equals(obj2) ? 1.0 : 0.0;
		}
	}

	/* (non-Javadoc)
	 * @see org.evosuite.cfg.instrumentation.mutation.MutationOperator#isApplicable(org.evosuite.cfg.BytecodeInstruction)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(BytecodeInstruction instruction) {
		return instruction.getASMNode().getOpcode() == Opcodes.GETFIELD
		        || instruction.getASMNode().getOpcode() == Opcodes.GETSTATIC;

	}

}
