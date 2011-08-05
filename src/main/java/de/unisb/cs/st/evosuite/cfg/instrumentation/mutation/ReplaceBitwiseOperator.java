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
public class ReplaceBitwiseOperator implements MutationOperator {

	private static Set<Integer> opcodesInt = new HashSet<Integer>();

	private static Set<Integer> opcodesLong = new HashSet<Integer>();

	static {
		opcodesInt.addAll(Arrays.asList(new Integer[] { Opcodes.IAND, Opcodes.IOR,
		        Opcodes.IXOR, Opcodes.ISHL, Opcodes.ISHR, Opcodes.IUSHR }));

		opcodesLong.addAll(Arrays.asList(new Integer[] { Opcodes.LAND, Opcodes.LOR,
		        Opcodes.LXOR, Opcodes.LSHL, Opcodes.LSHR, Opcodes.LUSHR }));
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction) {

		// TODO: Check if this operator is applicable at all first
		// Should we do this via a method defined in the interface?
		InsnNode node = (InsnNode) instruction.getASMNode();

		List<Mutation> mutations = new LinkedList<Mutation>();
		Set<Integer> replacement = new HashSet<Integer>();
		if (opcodesInt.contains(node.getOpcode()))
			replacement.addAll(opcodesInt);
		else if (opcodesLong.contains(node.getOpcode()))
			replacement.addAll(opcodesLong);
		replacement.remove(node.getOpcode());

		for (int opcode : replacement) {

			InsnNode mutation = new InsnNode(opcode);
			// insert mutation into pool
			Mutation mutationObject = MutationPool.addMutation(className,
			                                                   methodName,
			                                                   "ReplaceBitwiseOperator",
			                                                   instruction,
			                                                   mutation,
			                                                   Mutation.getDefaultInfectionDistance());
			mutations.add(mutationObject);
		}

		return mutations;
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

		return false;
	}
}
