package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

public class Use extends DefUse {

	
	public Use(CFGVertex v) {
		if(!v.isUse())
			throw new IllegalArgumentException("Vertex of a use expected");
		
		this.v = v;
	}
}
