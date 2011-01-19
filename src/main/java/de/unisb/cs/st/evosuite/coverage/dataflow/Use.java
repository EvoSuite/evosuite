package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

/**
 * An object of this class corresponds to a Use inside the class under test.
 * 
 * Uses are created by the CFGMethodAdapter via the DefUsePool.
 * Each Use holds its corresponding CFGVertex from the ControlFlowGraph.
 * 
 * @author Andre Mis
 */

public class Use extends DefUse {

	
	public Use(CFGVertex v) {
		if(!v.isUse())
			throw new IllegalArgumentException("Vertex of a use expected");
		
		this.v = v;
	}
}
