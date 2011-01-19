package de.unisb.cs.st.evosuite.coverage.dataflow;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

public class Definition extends DefUse {

	
	public Definition(CFGVertex v) {
		if(!v.isDefinition())
			throw new IllegalArgumentException("Vertex of a definition expected");
		
		this.v = v;
	}
}
