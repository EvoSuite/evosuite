package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;

/**
 * An object of this class corresponds to a Definition inside the class under test.
 * 
 * Definitions are created by the CFGMethodAdapter via the DefUsePool.
 * Each Definition holds its corresponding CFGVertex from the ControlFlowGraph.
 * 
 * @author Andre Mis
 */

public class Definition extends DefUse {

	// TODO decide casting versus this constructor approach - that in this specific case i weirdly like
	public Definition(BytecodeInstruction v) {
		super(v);
		if(v==null)
			throw new IllegalArgumentException("null given");
		if(!isDefinition()) // TODO
			throw new IllegalArgumentException("Vertex of a definition expected");
	}
}
