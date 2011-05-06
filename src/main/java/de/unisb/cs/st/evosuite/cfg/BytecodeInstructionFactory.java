package de.unisb.cs.st.evosuite.cfg;

import org.objectweb.asm.tree.AbstractInsnNode;

import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUse;

public class BytecodeInstructionFactory {

	private static final BytecodeInstructionFactory instance = new BytecodeInstructionFactory();
	
	public static BytecodeInstructionFactory getInstance() {
		return instance;
	}
	
	public static BytecodeInstruction createBytecodeInstruction(
			String className, String methodName, int instructionId, AbstractInsnNode node) {

		AbstractInsnWrapper wrap = new AbstractInsnWrapper(className, methodName, instructionId, node);
		if(wrap.isActualBranch())
			return new Branch(wrap);
		if(wrap.isDefUse())
			return new DefUse(wrap);

		// TODO fix other coverage criteria URGENT
//		throw new NotImplementedException("Andre Mis");
		
		return new BytecodeInstruction(wrap);
	}
	
	private BytecodeInstructionFactory() {}
}
