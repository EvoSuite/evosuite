/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation.mutation;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import de.unisb.cs.st.evosuite.coverage.mutation.Mutation;
import de.unisb.cs.st.evosuite.coverage.mutation.MutationPool;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

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
		List<InsnList> mutationCode = new LinkedList<InsnList>();
		List<String> descriptions = new LinkedList<String>();

		if (instruction.getASMNode() instanceof VarInsnNode) {
			InsnList mutation = new InsnList();
			VarInsnNode node = (VarInsnNode) instruction.getASMNode();

			// insert mutation into bytecode with conditional
			mutation.add(new VarInsnNode(node.getOpcode(), node.var));
			mutation.add(new InsnNode(getNegation(node.getOpcode())));
			mutationCode.add(mutation);
			descriptions.add("Negation");

			if (node.getOpcode() == Opcodes.ILOAD) {
				mutation = new InsnList();
				mutation.add(new IincInsnNode(node.var, 1));
				mutation.add(new VarInsnNode(node.getOpcode(), node.var));
				descriptions.add("IINC 1");
				mutationCode.add(mutation);

				mutation = new InsnList();
				mutation.add(new IincInsnNode(node.var, -1));
				mutation.add(new VarInsnNode(node.getOpcode(), node.var));
				descriptions.add("IINC -1");
				mutationCode.add(mutation);
			}
		} else {
			InsnList mutation = new InsnList();
			FieldInsnNode node = (FieldInsnNode) instruction.getASMNode();
			Type type = Type.getType(node.desc);
			mutation.add(new FieldInsnNode(node.getOpcode(), node.owner, node.name,
			        node.desc));
			mutation.add(new InsnNode(getNegation(type)));
			descriptions.add("Negation");
			mutationCode.add(mutation);

			if (type == Type.INT_TYPE) {
				mutation = new InsnList();
				mutation.add(new FieldInsnNode(node.getOpcode(), node.owner, node.name,
				        node.desc));
				mutation.add(new InsnNode(Opcodes.ICONST_1));
				mutation.add(new InsnNode(Opcodes.IADD));
				descriptions.add("+1");
				mutationCode.add(mutation);

				mutation = new InsnList();
				mutation.add(new FieldInsnNode(node.getOpcode(), node.owner, node.name,
				        node.desc));
				mutation.add(new InsnNode(Opcodes.ICONST_M1));
				mutation.add(new InsnNode(Opcodes.IADD));
				descriptions.add("-1");
				mutationCode.add(mutation);
			}
		}

		int i = 0;
		for (InsnList mutation : mutationCode) {
			// insert mutation into pool
			Mutation mutationObject = MutationPool.addMutation(className,
			                                                   methodName,
			                                                   "InsertUnaryOp "
			                                                           + descriptions.get(i++),
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
		switch (node.getOpcode()) {
		case Opcodes.ILOAD:
		case Opcodes.LLOAD:
		case Opcodes.FLOAD:
		case Opcodes.DLOAD:
			return true;
		case Opcodes.GETFIELD:
		case Opcodes.GETSTATIC:
			FieldInsnNode fieldNode = (FieldInsnNode) instruction.getASMNode();
			Type type = Type.getType(fieldNode.desc);
			if (type == Type.BYTE_TYPE || type == Type.SHORT_TYPE
			        || type == Type.LONG_TYPE || type == Type.FLOAT_TYPE
			        || type == Type.DOUBLE_TYPE || type == Type.BOOLEAN_TYPE
			        || type == Type.INT_TYPE) {
				return true;
			}
		default:
			return false;
		}
	}

	private int getNegation(Type type) {
		if (type.equals(Type.BYTE_TYPE)) {
			return Opcodes.INEG;
		} else if (type == Type.SHORT_TYPE) {
			return Opcodes.INEG;
		} else if (type == Type.LONG_TYPE) {
			return Opcodes.LNEG;
		} else if (type == Type.FLOAT_TYPE) {
			return Opcodes.FNEG;
		} else if (type == Type.DOUBLE_TYPE) {
			return Opcodes.DNEG;
		} else if (type == Type.BOOLEAN_TYPE) {
			return Opcodes.INEG;
		} else if (type == Type.INT_TYPE) {
			return Opcodes.INEG;
		} else {
			throw new RuntimeException("Don't know how to negate type " + type);
		}
	}

	private int getNegation(int opcode) {
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
