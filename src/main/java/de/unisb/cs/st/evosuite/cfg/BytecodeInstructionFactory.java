package de.unisb.cs.st.evosuite.cfg;

import org.objectweb.asm.tree.AbstractInsnNode;

public class BytecodeInstructionFactory {

	public static BytecodeInstruction createBytecodeInstruction(String className,
	        String methodName, int instructionId, int jpfId, AbstractInsnNode node) {

		BytecodeInstruction instruction = new BytecodeInstruction(className, methodName,
		        instructionId, jpfId, node);

		return instruction;
	}

}
