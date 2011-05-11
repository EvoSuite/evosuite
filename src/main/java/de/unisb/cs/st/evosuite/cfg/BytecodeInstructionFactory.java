package de.unisb.cs.st.evosuite.cfg;

import org.objectweb.asm.tree.AbstractInsnNode;

import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUse;

public class BytecodeInstructionFactory {

	public static BytecodeInstruction createBytecodeInstruction(
			String className, String methodName, int instructionId,
			AbstractInsnNode node) {

		// TODO fix other coverage criteria URGENT

		return new BytecodeInstruction(className, methodName, instructionId,
				node);
	}

}
