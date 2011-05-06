package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;

/**
 * An object of this class corresponds to a Use inside the class under test.
 * 
 * Uses are created by the CFGMethodAdapter via the DefUsePool.
 * Each Use holds its corresponding CFGVertex from the ControlFlowGraph.
 * 
 * @author Andre Mis
 */

public class Use extends DefUse {

	// TODO decide casting versus this constructor approach - that in this specific case i weirdly like
	public Use(BytecodeInstruction v) {
		super(v);
		if(!isUse())
			throw new IllegalArgumentException("Vertex of a use expected");
	}
	
}
