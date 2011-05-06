package de.unisb.cs.st.evosuite.coverage.branch;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;

/**
 * An object of this class corresponds to a Branch inside the class under test.
 * 
 * Branches are created by the CFGMethodAdapter via the BranchPool. Each Branch
 * holds its corresponding CFGVertex from the ControlFlowGraph.
 * 
 * @author Andre Mis
 */
public class Branch extends BytecodeInstruction {

	// TODO decide casting versus this constructor approach - that in this specific case i weirdly like
	public Branch(BytecodeInstruction wrapper) {
		super(wrapper);
		if(!isActualBranch())
			throw new IllegalArgumentException("only actual branch instructions are accepted");
		
	}
	
}
