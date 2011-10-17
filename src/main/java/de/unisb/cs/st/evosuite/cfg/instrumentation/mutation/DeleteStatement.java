/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation.mutation;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
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
 * @author Gordon Fraser
 * 
 */
public class DeleteStatement implements MutationOperator {

	private static Logger logger = LoggerFactory.getLogger(DeleteStatement.class);

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.MutationOperator#apply(org.objectweb.asm.tree.MethodNode, java.lang.String, java.lang.String, de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public List<Mutation> apply(MethodNode mn, String className, String methodName,
	        BytecodeInstruction instruction) {

		List<Mutation> mutations = new LinkedList<Mutation>();

		MethodInsnNode node = (MethodInsnNode) instruction.getASMNode();
		Type returnType = Type.getReturnType(node.desc);

		// insert mutation into bytecode with conditional
		InsnList mutation = new InsnList();
		logger.debug("Mutation deletestatement for statement " + node.name + node.desc);
		for (Type argType : Type.getArgumentTypes(node.desc)) {
			if (argType.getSize() == 0)
				logger.debug("Ignoring parameter of type " + argType);
			else if (argType.getSize() == 2) {
				mutation.insert(new InsnNode(Opcodes.POP2));
				logger.debug("Deleting parameter of 2 type " + argType);
			} else {
				logger.debug("Deleting parameter of 1 type " + argType);
				mutation.insert(new InsnNode(Opcodes.POP));
			}
		}
		if (node.getOpcode() == Opcodes.INVOKEVIRTUAL) {
			logger.debug("Deleting callee of type " + node.owner);
			mutation.add(new InsnNode(Opcodes.POP));
		} else if (node.getOpcode() == Opcodes.INVOKEINTERFACE) {
			try {
				Class<?> clazz = Class.forName(node.owner.replace("/", "."));
				if (!Modifier.isStatic(clazz.getModifiers())) {
					logger.debug("Deleting callee of type " + node.owner);
					mutation.add(new InsnNode(Opcodes.POP));
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mutation.add(getDefault(returnType));
		// insert mutation into pool
		Mutation mutationObject = MutationPool.addMutation(className,
		                                                   methodName,
		                                                   "DeleteStatement: "
		                                                           + node.name
		                                                           + node.desc,
		                                                   instruction,
		                                                   mutation,
		                                                   Mutation.getDefaultInfectionDistance());

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

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.instrumentation.mutation.MutationOperator#isApplicable(de.unisb.cs.st.evosuite.cfg.BytecodeInstruction)
	 */
	@Override
	public boolean isApplicable(BytecodeInstruction instruction) {
		return instruction.isMethodCall()
		        && instruction.getASMNode().getOpcode() != Opcodes.INVOKESPECIAL;
	}
}
