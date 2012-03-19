/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation.mutation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Frame;

import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationPool;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

/**
 * @author Gordon Fraser
 * 
 */
public class NegateCondition implements MutationOperator {

	private static Map<Integer, Integer> opcodeMap = new HashMap<Integer, Integer>();

	static {
		opcodeMap.put(Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE);
		opcodeMap.put(Opcodes.IF_ACMPNE, Opcodes.IF_ACMPEQ);
		opcodeMap.put(Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPNE);
		opcodeMap.put(Opcodes.IF_ICMPGE, Opcodes.IF_ICMPLT);
		opcodeMap.put(Opcodes.IF_ICMPGT, Opcodes.IF_ICMPLE);
		opcodeMap.put(Opcodes.IF_ICMPLE, Opcodes.IF_ICMPGT);
		opcodeMap.put(Opcodes.IF_ICMPLT, Opcodes.IF_ICMPGE);
		opcodeMap.put(Opcodes.IF_ICMPNE, Opcodes.IF_ICMPEQ);
		opcodeMap.put(Opcodes.IFEQ, Opcodes.IFNE);
		opcodeMap.put(Opcodes.IFGE, Opcodes.IFLT);
		opcodeMap.put(Opcodes.IFGT, Opcodes.IFLE);
		opcodeMap.put(Opcodes.IFLE, Opcodes.IFGT);
		opcodeMap.put(Opcodes.IFLT, Opcodes.IFGE);
		opcodeMap.put(Opcodes.IFNE, Opcodes.IFEQ);
		opcodeMap.put(Opcodes.IFNONNULL, Opcodes.IFNULL);
		opcodeMap.put(Opcodes.IFNULL, Opcodes.IFNONNULL);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction, Frame frame) {

		List<Mutation> mutations = new LinkedList<Mutation>();

		JumpInsnNode node = (JumpInsnNode) instruction.getASMNode();
		LabelNode target = node.label;

		// insert mutation into bytecode with conditional
		JumpInsnNode mutation = new JumpInsnNode(getOpposite(node.getOpcode()), target);
		// insert mutation into pool
		Mutation mutationObject = MutationPool.addMutation(className,
		                                                   methodName,
		                                                   "NegateCondition",
		                                                   instruction,
		                                                   mutation,
		                                                   Mutation.getDefaultInfectionDistance());

		mutations.add(mutationObject);
		return mutations;
	}

	private static int getOpposite(int opcode) {
		return opcodeMap.get(opcode);
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.MutationOperator#isApplicable(de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public boolean isApplicable(BytecodeInstruction instruction) {
		return instruction.isBranch();
	}

}
