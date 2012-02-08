/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation.mutation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationPool;

/**
 * @author fraser
 * 
 */
public class ReplaceComparisonOperator implements MutationOperator {

	private static final Logger logger = LoggerFactory.getLogger(ReplaceComparisonOperator.class);

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

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction) {
		JumpInsnNode node = (JumpInsnNode) instruction.getASMNode();
		List<Mutation> mutations = new LinkedList<Mutation>();
		LabelNode target = node.label;

		for (Integer op : getOperators(node.getOpcode())) {

			// insert mutation into bytecode with conditional
			JumpInsnNode mutation = new JumpInsnNode(op, target);
			// insert mutation into pool
			Mutation mutationObject = MutationPool.addMutation(className,
			                                                   methodName,
			                                                   "ReplaceComparisonOperator "
			                                                           + getOp(node.getOpcode())
			                                                           + " -> "
			                                                           + getOp(op),
			                                                   instruction,
			                                                   mutation,
			                                                   getInfectionDistance(node.getOpcode(),
			                                                                        op));
			mutations.add(mutationObject);
			String equiv = System.getProperty("EQUIVID");
			if (equiv != null) {
				int id = Integer.parseInt(equiv);
				if (mutationObject.getId() == id) {

					logger.info("Found equivalent mutation " + mutationObject);
					int num = Integer.parseInt(System.getProperty("NUM_INFEASIBLE"));
					for (int i = 1; i < num; i++) {
						// insert mutation into bytecode with conditional
						JumpInsnNode mutation2 = new JumpInsnNode(op, target);
						// insert mutation into pool
						Mutation mutationObject2 = MutationPool.addMutation(className,
						                                                    methodName,
						                                                    "ReplaceComparisonOperator "
						                                                            + getOp(node.getOpcode())
						                                                            + " -> "
						                                                            + getOp(op),
						                                                    instruction,
						                                                    mutation2,
						                                                    getInfectionDistance(node.getOpcode(),
						                                                                         op));
						mutations.add(mutationObject2);

					}
				}
			}
		}

		return mutations;
	}

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
			        "de/unisb/cs/st/evosuite/cfg/instrumentation/mutation/ReplaceComparisonOperator",
			        "getInfectionDistance", "(IIII)D"));
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
			        "de/unisb/cs/st/evosuite/cfg/instrumentation/mutation/ReplaceComparisonOperator",
			        "getInfectionDistance", "(III)D"));
			break;

		default:
			distance.add(new LdcInsnNode(0.0));
		}

		return distance;
	}

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
			}

		}

		throw new RuntimeException("Unknown operator replacement: " + opcodeOrig + " -> "
		        + opcodeNew);
	}

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
			}

		}

		throw new RuntimeException("Unknown operator replacement: " + opcodeOrig + " -> "
		        + opcodeNew);
	}

	private Set<Integer> getOperators(int opcode) {
		Set<Integer> replacement = new HashSet<Integer>();
		if (opcodesReference.contains(opcode))
			replacement.addAll(opcodesReference);
		else if (opcodesNull.contains(opcode))
			replacement.addAll(opcodesNull);
		else if (opcodesInt.contains(opcode))
			replacement.addAll(opcodesInt);
		else if (opcodesIntInt.contains(opcode))
			replacement.addAll(opcodesIntInt);
		replacement.remove(opcode);
		return replacement;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.MutationOperator#isApplicable(de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public boolean isApplicable(BytecodeInstruction instruction) {
		return instruction.isBranch();
	}

}
