/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation.mutation;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationPool;

/**
 * @author fraser
 * 
 */
public class InsertUnaryOperator implements MutationOperator {

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction) {
		// TODO - need to keep InsnList in Mutation, not only Instruction?

		// Mutation: Insert an INEG _after_ an iload 
		List<Mutation> mutations = new LinkedList<Mutation>();

		VarInsnNode node = (VarInsnNode) instruction.getASMNode();

		// insert mutation into bytecode with conditional
		InsnList mutation = new InsnList();
		mutation.add(new VarInsnNode(node.getOpcode(), node.var));
		mutation.add(new InsnNode(getNegation(node.getOpcode())));
		// insert mutation into pool
		Mutation mutationObject = MutationPool.addMutation(className,
		                                                   methodName,
		                                                   "InsertUnaryOp",
		                                                   instruction,
		                                                   mutation,
		                                                   Mutation.getDefaultInfectionDistance());

		mutations.add(mutationObject);
		return mutations;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.MutationOperator#isApplicable(de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public boolean isApplicable(BytecodeInstruction instruction) {
		AbstractInsnNode node = instruction.getASMNode();
		switch (node.getOpcode()) {
		case Opcodes.ILOAD:
		case Opcodes.LLOAD:
		case Opcodes.FLOAD:
		case Opcodes.DLOAD:
			return true;
		default:
			return false;
		}
	}

	public int getNegation(int opcode) {
		switch (opcode) {
		case Opcodes.ILOAD:
			return Opcodes.INEG;
		case Opcodes.LLOAD:
			return Opcodes.LNEG;
		case Opcodes.FLOAD:
			return Opcodes.FNEG;
		case Opcodes.DLOAD:
			return Opcodes.DNEG;
		default:
			throw new RuntimeException("Invalid opcode for negation: " + opcode);
		}
	}

}
