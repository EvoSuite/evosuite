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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.evosuite.PackageInfo;
import org.evosuite.coverage.mutation.Mutation;
import org.evosuite.coverage.mutation.MutationPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.instrumentation.BooleanValueInterpreter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>ReplaceComparisonOperator class.</p>
 *
 * @author fraser
 */
public class ReplaceComparisonOperator implements MutationOperator {

	private static final Logger logger = LoggerFactory.getLogger(ReplaceComparisonOperator.class);

	public static final String NAME = "ReplaceComparisonOperator";
	
	private static Set<Integer> opcodesReference = new HashSet<Integer>();

	private static Set<Integer> opcodesNull = new HashSet<Integer>();

	private static Set<Integer> opcodesInt = new HashSet<Integer>();

	private static Set<Integer> opcodesIntInt = new HashSet<Integer>();

	static {
		opcodesReference.addAll(Arrays.asList(new Integer[] { Opcodes.IF_ACMPEQ,
		        Opcodes.IF_ACMPNE }));
		opcodesNull.addAll(Arrays.asList(new Integer[] { Opcodes.IFNULL,
		        Opcodes.IFNONNULL }));
		opcodesInt.addAll(Arrays.asList(new Integer[] { Opcodes.IFEQ, Opcodes.IFNE,
		        Opcodes.IFGE, Opcodes.IFGT, Opcodes.IFLE, Opcodes.IFLT }));
		opcodesIntInt.addAll(Arrays.asList(new Integer[] { Opcodes.IF_ICMPEQ,
		        Opcodes.IF_ICMPGE, Opcodes.IF_ICMPGT, Opcodes.IF_ICMPLE,
		        Opcodes.IF_ICMPLT, Opcodes.IF_ICMPNE }));

	}

	private String getOp(int opcode) {
		switch (opcode) {
		case Opcodes.IFEQ:
		case Opcodes.IF_ACMPEQ:
		case Opcodes.IF_ICMPEQ:
			return "==";
		case Opcodes.IFNE:
		case Opcodes.IF_ACMPNE:
		case Opcodes.IF_ICMPNE:
			return "!=";
		case Opcodes.IFLT:
		case Opcodes.IF_ICMPLT:
			return "<";
		case Opcodes.IFLE:
		case Opcodes.IF_ICMPLE:
			return "<=";
		case Opcodes.IFGT:
		case Opcodes.IF_ICMPGT:
			return ">";
		case Opcodes.IFGE:
		case Opcodes.IF_ICMPGE:
			return ">=";
		case Opcodes.IFNULL:
			return "= null";
		case Opcodes.IFNONNULL:
			return "!= null";
		}
		throw new RuntimeException("Unknown opcode: " + opcode);
	}

	private static final int TRUE = -1;

	private static final int FALSE = -2;

	/* (non-Javadoc)
	 * @see org.evosuite.cfg.instrumentation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, org.evosuite.cfg.BytecodeInstruction)
	 */
	/** {@inheritDoc} */
	@Override
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction, Frame frame) {
		JumpInsnNode node = (JumpInsnNode) instruction.getASMNode();
		List<Mutation> mutations = new LinkedList<Mutation>();
		LabelNode target = node.label;

		boolean isBoolean = frame.getStack(frame.getStackSize() - 1) == BooleanValueInterpreter.BOOLEAN_VALUE;

		for (Integer op : getOperators(node.getOpcode(), isBoolean)) {
			logger.debug("Adding replacement " + op);
			if (op >= 0) {
				// insert mutation into bytecode with conditional
				JumpInsnNode mutation = new JumpInsnNode(op, target);
				// insert mutation into pool
				Mutation mutationObject = MutationPool.addMutation(className,
				                                                   methodName,
				                                                   NAME + " "
				                                                           + getOp(node.getOpcode())
				                                                           + " -> "
				                                                           + getOp(op),
				                                                   instruction,
				                                                   mutation,
				                                                   getInfectionDistance(node.getOpcode(),
				                                                                        op));
				mutations.add(mutationObject);
			} else {
				// Replace relational operator with TRUE/FALSE

				InsnList mutation = new InsnList();
				if (opcodesInt.contains(node.getOpcode()))
					mutation.add(new InsnNode(Opcodes.POP));
				else
					mutation.add(new InsnNode(Opcodes.POP2));
				if (op == TRUE) {
					mutation.add(new LdcInsnNode(1));
				} else {
					mutation.add(new LdcInsnNode(0));
				}
				mutation.add(new JumpInsnNode(Opcodes.IFNE, target));
				Mutation mutationObject = MutationPool.addMutation(className,
				                                                   methodName,
				                                                   NAME + " "
				                                                           + getOp(node.getOpcode())
				                                                           + " -> " + op,
				                                                   instruction,
				                                                   mutation,
				                                                   getInfectionDistance(node.getOpcode(),
				                                                                        op));
				mutations.add(mutationObject);
			}
		}

		return mutations;
	}

	/**
	 * <p>getInfectionDistance</p>
	 *
	 * @param opcodeOrig a int.
	 * @param opcodeNew a int.
	 * @return a {@link org.objectweb.asm.tree.InsnList} object.
	 */
	public InsnList getInfectionDistance(int opcodeOrig, int opcodeNew) {
		InsnList distance = new InsnList();
		switch (opcodeOrig) {
		case Opcodes.IF_ICMPEQ:
		case Opcodes.IF_ICMPNE:
		case Opcodes.IF_ICMPLE:
		case Opcodes.IF_ICMPLT:
		case Opcodes.IF_ICMPGE:
		case Opcodes.IF_ICMPGT:
			distance.add(new InsnNode(Opcodes.DUP2));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
					PackageInfo.getNameWithSlash(ReplaceComparisonOperator.class),
			        "getInfectionDistance", "(IIII)D", false));
			break;

		case Opcodes.IFEQ:
		case Opcodes.IFNE:
		case Opcodes.IFLE:
		case Opcodes.IFLT:
		case Opcodes.IFGE:
		case Opcodes.IFGT:
			distance.add(new InsnNode(Opcodes.DUP));
			distance.add(new LdcInsnNode(opcodeOrig));
			distance.add(new LdcInsnNode(opcodeNew));
			distance.add(new MethodInsnNode(
			        Opcodes.INVOKESTATIC,
					PackageInfo.getNameWithSlash(ReplaceComparisonOperator.class),
			        "getInfectionDistance", "(III)D", false));
			break;

		default:
			distance.add(new LdcInsnNode(0.0));
		}

		return distance;
	}

	/**
	 * <p>getInfectionDistance</p>
	 *
	 * @param left a int.
	 * @param right a int.
	 * @param opcodeOrig a int.
	 * @param opcodeNew a int.
	 * @return a double.
	 */
	public static double getInfectionDistance(int left, int right, int opcodeOrig,
	        int opcodeNew) {
		long val = (long) left - (long) right;
		switch (opcodeOrig) {
		case Opcodes.IF_ICMPLT:
			switch (opcodeNew) {
			case Opcodes.IF_ICMPLE:
				// Only differs for val == 0
				return Math.abs(val);
			case Opcodes.IF_ICMPEQ:
				// Only differs for val <= 0
				return val > 0 ? val : 0.0;
			case Opcodes.IF_ICMPGT:
				// Only same for val == 0
				return val == 0 ? 1.0 : 0.0;
			case Opcodes.IF_ICMPGE:
				// Always differs
				return 0.0;
			case Opcodes.IF_ICMPNE:
				// Only differs for val > 0
				return val <= 0 ? Math.abs(val) + 1.0 : 0.0;
			case TRUE:
				return val < 0 ? 1.0 : 0.0;
			case FALSE:
				return val < 0 ? 0.0 : 1.0;
			}
		case Opcodes.IF_ICMPLE:
			switch (opcodeNew) {
			case Opcodes.IF_ICMPLT:
				// Only differs for val == 0
				return Math.abs(val);
			case Opcodes.IF_ICMPEQ:
				return val >= 0 ? val + 1.0 : 0.0;
			case Opcodes.IF_ICMPGE:
				// Only equals for val == 0
				return val == 0 ? 1.0 : 0.0;
			case Opcodes.IF_ICMPGT:
				// Always differs
				return 0.0;
			case Opcodes.IF_ICMPNE:
				// Only differs if val >= 0
				return val < 0 ? Math.abs(val) : 0.0;
			case TRUE:
				return val <= 0 ? 1.0 : 0.0;
			case FALSE:
				return val <= 0 ? 0.0 : 1.0;

			}
		case Opcodes.IF_ICMPGT:
			switch (opcodeNew) {
			case Opcodes.IF_ICMPGE:
				// Only differs for val == 0
				return Math.abs(val);
			case Opcodes.IF_ICMPEQ:
				// Only differs for val >= 0
				return val < 0 ? Math.abs(val) : 0.0;
			case Opcodes.IF_ICMPLT:
				// Only same for val == 0
				return val == 0 ? 1.0 : 0.0;
			case Opcodes.IF_ICMPLE:
				// Always differs
				return 0.0;
			case Opcodes.IF_ICMPNE:
				// Only differs for val < 0
				return val >= 0 ? val + 1.0 : 0.0;
			case TRUE:
				return val > 0 ? 1.0 : 0.0;
			case FALSE:
				return val > 0 ? 0.0 : 1.0;
			}
		case Opcodes.IF_ICMPGE:
			switch (opcodeNew) {
			case Opcodes.IF_ICMPGT:
				// Only differs for val == 0
				return Math.abs(val);
			case Opcodes.IF_ICMPEQ:
				return val <= 0 ? Math.abs(val) + 1.0 : 0.0;
			case Opcodes.IF_ICMPLE:
				// Only equals for val == 0
				return val == 0 ? 1.0 : 0.0;
			case Opcodes.IF_ICMPLT:
				// Always differs
				return 0.0;
			case Opcodes.IF_ICMPNE:
				// Only differs if val > 0
				return val > 0 ? val : 0.0;
			case TRUE:
				return val >= 0 ? 1.0 : 0.0;
			case FALSE:
				return val >= 0 ? 0.0 : 1.0;
			}
		case Opcodes.IF_ICMPEQ:
			switch (opcodeNew) {
			case Opcodes.IF_ICMPLT:
				// Only differs if val <= 0
				return val > 0 ? val : 0.0;
			case Opcodes.IF_ICMPGT:
				// Only differs if val >= 0
				return val < 0 ? Math.abs(val) : 0.0;
			case Opcodes.IF_ICMPNE:
				// Always differs
				return 0.0;
			case Opcodes.IF_ICMPLE:
				return val >= 0 ? val + 1.0 : 0.0;
			case Opcodes.IF_ICMPGE:
				return val <= 0 ? Math.abs(val) + 1.0 : 0.0;
			case TRUE:
				return val == 0 ? 1.0 : 0.0;
			case FALSE:
				return val == 0 ? 0.0 : 1.0;
			}
		case Opcodes.IF_ICMPNE:
			switch (opcodeNew) {
			case Opcodes.IF_ICMPEQ:
				return 0.0;
			case Opcodes.IF_ICMPLT:
				// Only differs for val > 0
				return val <= 0 ? Math.abs(val) + 1.0 : 0.0;
			case Opcodes.IF_ICMPLE:
				// Only differs for val > 0
				return val < 0 ? Math.abs(val) : 0.0;
			case Opcodes.IF_ICMPGT:
				return val >= 0 ? val + 1.0 : 0.0;
			case Opcodes.IF_ICMPGE:
				return val > 0 ? val : 0.0;
			case TRUE:
				return val != 0 ? 1.0 : 0.0;
			case FALSE:
				return val != 0 ? 0.0 : 1.0;
			}
		}

		throw new RuntimeException("Unknown operator replacement: " + opcodeOrig + " -> "
		        + opcodeNew);
	}

	/**
	 * <p>getInfectionDistance</p>
	 *
	 * @param intVal a int.
	 * @param opcodeOrig a int.
	 * @param opcodeNew a int.
	 * @return a double.
	 */
	public static double getInfectionDistance(int intVal, int opcodeOrig, int opcodeNew) {
		long val = intVal;
		switch (opcodeOrig) {
		case Opcodes.IFLT:
			switch (opcodeNew) {
			case Opcodes.IFLE:
				// Only differs for val == 0
				return Math.abs(val);
			case Opcodes.IFEQ:
				// Only differs for val <= 0
				return val > 0 ? val : 0.0;
			case Opcodes.IFGT:
				// Only same for val == 0
				return val == 0 ? 1.0 : 0.0;
			case Opcodes.IFGE:
				// Always differs
				return 0.0;
			case Opcodes.IFNE:
				// Only differs for val > 0
				return val <= 0 ? Math.abs(val) + 1.0 : 0.0;
			case TRUE:
				return val < 0 ? 1.0 : 0.0;
			case FALSE:
				return val < 0 ? 0.0 : 1.0;
			}
		case Opcodes.IFLE:
			switch (opcodeNew) {
			case Opcodes.IFLT:
				// Only differs for val == 0
				return Math.abs(val);
			case Opcodes.IFEQ:
				return val >= 0 ? val + 1.0 : 0.0;
			case Opcodes.IFGE:
				// Only equals for val == 0
				return val == 0 ? 1.0 : 0.0;
			case Opcodes.IFGT:
				// Always differs
				return 0.0;
			case Opcodes.IFNE:
				// Only differs if val >= 0
				return val < 0 ? Math.abs(val) : 0.0;
			case TRUE:
				return val <= 0 ? 1.0 : 0.0;
			case FALSE:
				return val <= 0 ? 0.0 : 1.0;
			}
		case Opcodes.IFGT:
			switch (opcodeNew) {
			case Opcodes.IFGE:
				// Only differs for val == 0
				return Math.abs(val);
			case Opcodes.IFEQ:
				// Only differs for val >= 0
				return val < 0 ? Math.abs(val) : 0.0;
			case Opcodes.IFLT:
				// Only same for val == 0
				return val == 0 ? 1.0 : 0.0;
			case Opcodes.IFLE:
				// Always differs
				return 0.0;
			case Opcodes.IFNE:
				// Only differs for val < 0
				return val >= 0 ? val + 1.0 : 0.0;
			case TRUE:
				return val > 0 ? 1.0 : 0.0;
			case FALSE:
				return val > 0 ? 0.0 : 1.0;
			}
		case Opcodes.IFGE:
			switch (opcodeNew) {
			case Opcodes.IFGT:
				// Only differs for val == 0
				return Math.abs(val);
			case Opcodes.IFEQ:
				return val <= 0 ? Math.abs(val) + 1.0 : 0.0;
			case Opcodes.IFLE:
				// Only equals for val == 0
				return val == 0 ? 1.0 : 0.0;
			case Opcodes.IFLT:
				// Always differs
				return 0.0;
			case Opcodes.IFNE:
				// Only differs if val > 0
				return val > 0 ? val : 0.0;
			case TRUE:
				return val >= 0 ? 1.0 : 0.0;
			case FALSE:
				return val >= 0 ? 0.0 : 1.0;
			}
		case Opcodes.IFEQ:
			switch (opcodeNew) {
			case Opcodes.IFLT:
				// Only differs if val <= 0
				return val > 0 ? val : 0.0;
			case Opcodes.IFGT:
				// Only differs if val >= 0
				return val < 0 ? Math.abs(val) : 0.0;
			case Opcodes.IFNE:
				// Always differs
				return 0.0;
			case Opcodes.IFLE:
				return val >= 0 ? val + 1.0 : 0.0;
			case Opcodes.IFGE:
				return val <= 0 ? Math.abs(val) + 1.0 : 0.0;
			case TRUE:
				return val == 0 ? 1.0 : 0.0;
			case FALSE:
				return val == 0 ? 0.0 : 1.0;
			}
		case Opcodes.IFNE:
			switch (opcodeNew) {
			case Opcodes.IFEQ:
				return 0.0;
			case Opcodes.IFLT:
				// Only differs for val > 0
				return val <= 0 ? Math.abs(val) + 1.0 : 0.0;
			case Opcodes.IFLE:
				// Only differs for val > 0
				return val < 0 ? Math.abs(val) : 0.0;
			case Opcodes.IFGT:
				return val >= 0 ? val + 1.0 : 0.0;
			case Opcodes.IFGE:
				return val > 0 ? val : 0.0;
			case TRUE:
				return val != 0 ? 1.0 : 0.0;
			case FALSE:
				return val != 0 ? 0.0 : 1.0;
			}

		}

		throw new RuntimeException("Unknown operator replacement: " + opcodeOrig + " -> "
		        + opcodeNew);
	}

	private Set<Integer> getOperators(int opcode, boolean isBoolean) {
		Set<Integer> replacement = new HashSet<Integer>();
		if (opcodesReference.contains(opcode))
			replacement.addAll(opcodesReference);
		else if (opcodesNull.contains(opcode))
			replacement.addAll(opcodesNull);
		else if (opcodesInt.contains(opcode)) {
			if (isBoolean) {
				replacement.add(getBooleanIntReplacement(opcode));
			} else {
				replacement.addAll(getIntReplacement(opcode));
			}
			// replacement.addAll(opcodesInt);
		} else if (opcodesIntInt.contains(opcode)) {
			if (isBoolean) {
				replacement.add(getBooleanIntIntReplacement(opcode));
			} else {
				replacement.addAll(getIntIntReplacement(opcode));
			}
			// replacement.addAll(opcodesIntInt);
		}
		replacement.remove(opcode);
		return replacement;
	}

	private int getBooleanIntReplacement(int opcode) {
		logger.debug("Getting Boolean int replacement");
		switch (opcode) {
		case Opcodes.IFEQ:
			return Opcodes.IFNE;
		case Opcodes.IFNE:
			return Opcodes.IFEQ;
		case Opcodes.IFGT:
			return Opcodes.IFEQ;
		case Opcodes.IFLE:
			return Opcodes.IFGT;
			// The rest should not occur except if our interpreter did something wrong
		case Opcodes.IFGE:
			return Opcodes.IFLT;
		case Opcodes.IFLT:
			return Opcodes.IFGE;
		}
		throw new RuntimeException("Illegal opcode received: " + opcode);
	}

	private int getBooleanIntIntReplacement(int opcode) {
		logger.debug("Getting Boolean int int replacement");

		switch (opcode) {
		case Opcodes.IF_ICMPEQ:
			return Opcodes.IF_ICMPNE;
		case Opcodes.IF_ICMPNE:
			return Opcodes.IF_ICMPEQ;
		case Opcodes.IF_ICMPGT:
			return Opcodes.IF_ICMPEQ;
		case Opcodes.IF_ICMPLE:
			return Opcodes.IF_ICMPGT;
			// The rest should not occur except if our interpreter did something wrong
		case Opcodes.IF_ICMPGE:
			return Opcodes.IF_ICMPLT;
		case Opcodes.IF_ICMPLT:
			return Opcodes.IF_ICMPGE;
		}
		throw new RuntimeException("Illegal opcode received: " + opcode);
	}

	private Set<Integer> getIntReplacement(int opcode) {
		logger.debug("Getting int replacement");

		Set<Integer> replacement = new HashSet<Integer>();
		switch (opcode) {
		case Opcodes.IFEQ:
			replacement.add(Opcodes.IFGE);
			replacement.add(Opcodes.IFLE);
			replacement.add(FALSE);
			// False
			break;
		case Opcodes.IFNE:
			replacement.add(Opcodes.IFLT);
			replacement.add(Opcodes.IFGT);
			// True
			replacement.add(TRUE);
			break;
		case Opcodes.IFGT:
			replacement.add(Opcodes.IFGE);
			replacement.add(Opcodes.IFNE);
			// False
			replacement.add(FALSE);
			break;
		case Opcodes.IFLE:
			replacement.add(Opcodes.IFLT);
			replacement.add(Opcodes.IFEQ);
			// True
			replacement.add(TRUE);
			break;
		case Opcodes.IFGE:
			replacement.add(Opcodes.IFGT);
			replacement.add(Opcodes.IFEQ);
			// True
			replacement.add(TRUE);
			break;
		case Opcodes.IFLT:
			replacement.add(Opcodes.IFLE);
			replacement.add(Opcodes.IFNE);
			// False
			replacement.add(FALSE);
			break;
		}
		return replacement;
	}

	private Set<Integer> getIntIntReplacement(int opcode) {
		logger.info("Getting int int replacement");

		Set<Integer> replacement = new HashSet<Integer>();
		switch (opcode) {
		case Opcodes.IF_ICMPEQ:
			replacement.add(Opcodes.IF_ICMPGE);
			replacement.add(Opcodes.IF_ICMPLE);
			// False
			replacement.add(FALSE);
			break;
		case Opcodes.IF_ICMPNE:
			replacement.add(Opcodes.IF_ICMPLT);
			replacement.add(Opcodes.IF_ICMPGT);
			// True
			replacement.add(TRUE);
			break;
		case Opcodes.IF_ICMPGT:
			replacement.add(Opcodes.IF_ICMPGE);
			replacement.add(Opcodes.IF_ICMPNE);
			// False
			replacement.add(FALSE);
			break;
		case Opcodes.IF_ICMPLE:
			replacement.add(Opcodes.IF_ICMPLT);
			replacement.add(Opcodes.IF_ICMPEQ);
			// True
			replacement.add(TRUE);
			break;
		case Opcodes.IF_ICMPGE:
			replacement.add(Opcodes.IF_ICMPGT);
			replacement.add(Opcodes.IF_ICMPEQ);
			// True
			replacement.add(TRUE);
			break;
		case Opcodes.IF_ICMPLT:
			replacement.add(Opcodes.IF_ICMPLE);
			replacement.add(Opcodes.IF_ICMPNE);
			// False
			replacement.add(FALSE);
			break;
		}
		return replacement;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.cfg.instrumentation.mutation.MutationOperator#isApplicable(org.evosuite.cfg.BytecodeInstruction)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(BytecodeInstruction instruction) {
		return instruction.isBranch();
	}

}
