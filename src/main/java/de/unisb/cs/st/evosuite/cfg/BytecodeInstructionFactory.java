package de.unisb.cs.st.evosuite.cfg;

import org.objectweb.asm.tree.AbstractInsnNode;

public class BytecodeInstructionFactory {

	public static BytecodeInstruction createBytecodeInstruction(
			String className, String methodName, int instructionId,
			AbstractInsnNode node) {

		return new BytecodeInstruction(className, methodName, instructionId,
				node);
	}

}
