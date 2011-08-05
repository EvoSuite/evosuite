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
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
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
			                                                   Mutation.getDefaultInfectionDistance());
			mutations.add(mutationObject);
		}

		return mutations;
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
