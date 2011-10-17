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

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationPool;

/**
 * @author fraser
 * 
 */
public class ReplaceComparisonOperator implements MutationOperator {

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
			                                                   "ReplaceComparisonOperator",
			                                                   instruction,
			                                                   mutation,
			                                                   getInfectionDistance(node.getOpcode(),
			                                                                        op));
			mutations.add(mutationObject);
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
		if ((opcodeOrig == Opcodes.IF_ICMPLT && opcodeNew == Opcodes.IF_ICMPLE)
		        || (opcodeOrig == Opcodes.IF_ICMPLE && opcodeNew == Opcodes.IF_ICMPLT)
		        || (opcodeOrig == Opcodes.IF_ICMPGT && opcodeNew == Opcodes.IF_ICMPGE)
		        || (opcodeOrig == Opcodes.IF_ICMPGE && opcodeNew == Opcodes.IF_ICMPGT)) {

			return Math.abs(left - right);
		}
		return 0.0;
	}

	public static double getInfectionDistance(int val, int opcodeOrig, int opcodeNew) {
		if ((opcodeOrig == Opcodes.IFLE && opcodeNew == Opcodes.IFLT)
		        || (opcodeOrig == Opcodes.IFLT && opcodeNew == Opcodes.IFLE)
		        || (opcodeOrig == Opcodes.IFGT && opcodeNew == Opcodes.IFGE)
		        || (opcodeOrig == Opcodes.IFGE && opcodeNew == Opcodes.IFGT)) {
			return Math.abs(val);
		}
		return 0.0;
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
