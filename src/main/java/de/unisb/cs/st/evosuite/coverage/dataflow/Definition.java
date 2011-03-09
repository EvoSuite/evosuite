package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

/**
 * An object of this class corresponds to a Definition inside the class under test.
 * 
 * Definitions are created by the CFGMethodAdapter via the DefUsePool.
 * Each Definition holds its corresponding CFGVertex from the ControlFlowGraph.
 * 
 * @author Andre Mis
 */

public class Definition extends DefUse {

	
	public Definition(CFGVertex v) {
		if(!v.isDefinition()) // TODO
			throw new IllegalArgumentException("Vertex of a definition expected");
		
		this.v = v;
	}
}
