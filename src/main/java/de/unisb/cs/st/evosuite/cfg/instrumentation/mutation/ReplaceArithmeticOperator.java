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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationPool;

/**
 * @author Gordon Fraser
 * 
 */
public class ReplaceArithmeticOperator implements MutationOperator {

	private static Set<Integer> opcodesInt = new HashSet<Integer>();

	private static Set<Integer> opcodesLong = new HashSet<Integer>();

	private static Set<Integer> opcodesFloat = new HashSet<Integer>();

	private static Set<Integer> opcodesDouble = new HashSet<Integer>();

	// TODO: Unary operators

	static {
		opcodesInt.addAll(Arrays.asList(new Integer[] { Opcodes.IADD, Opcodes.ISUB,
		        Opcodes.IMUL, Opcodes.IDIV, Opcodes.IREM }));

		opcodesLong.addAll(Arrays.asList(new Integer[] { Opcodes.LADD, Opcodes.LSUB,
		        Opcodes.LMUL, Opcodes.LDIV, Opcodes.LREM }));

		opcodesFloat.addAll(Arrays.asList(new Integer[] { Opcodes.FADD, Opcodes.FSUB,
		        Opcodes.FMUL, Opcodes.FDIV, Opcodes.FREM }));

		opcodesDouble.addAll(Arrays.asList(new Integer[] { Opcodes.DADD, Opcodes.DSUB,
		        Opcodes.DMUL, Opcodes.DDIV, Opcodes.DREM }));
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction) {

		List<Mutation> mutations = new LinkedList<Mutation>();

		InsnNode node = (InsnNode) instruction.getASMNode();

		for (int opcode : getMutations(node.getOpcode())) {

			InsnNode mutation = new InsnNode(opcode);
			// insert mutation into pool
			Mutation mutationObject = MutationPool.addMutation(className,
			                                                   methodName,
			                                                   "ReplaceArithmeticOperator",
			                                                   instruction,
			                                                   mutation,
			                                                   Mutation.getDefaultInfectionDistance());
			mutations.add(mutationObject);
		}

		return mutations;
	}

	private Set<Integer> getMutations(int opcode) {
		Set<Integer> replacement = new HashSet<Integer>();
		if (opcodesInt.contains(opcode))
			replacement.addAll(opcodesInt);
		else if (opcodesLong.contains(opcode))
			replacement.addAll(opcodesLong);
		else if (opcodesFloat.contains(opcode))
			replacement.addAll(opcodesFloat);
		else if (opcodesDouble.contains(opcode))
			replacement.addAll(opcodesDouble);

		replacement.remove(opcode);
		return replacement;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.MutationOperator#isApplicable(de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public boolean isApplicable(BytecodeInstruction instruction) {
		AbstractInsnNode node = instruction.getASMNode();
		int opcode = node.getOpcode();
		if (opcodesInt.contains(opcode))
			return true;
		else if (opcodesLong.contains(opcode))
			return true;
		else if (opcodesFloat.contains(opcode))
			return true;
		else if (opcodesDouble.contains(opcode))
			return true;

		return false;
	}
}
