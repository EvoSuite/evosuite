package de.unisb.cs.st.evosuite.graphs.ccfg;

import de.unisb.cs.st.evosuite.graphs.cfg.ControlFlowEdge;

public class CCFGCodeEdge extends CCFGEdge{

	private static final long serialVersionUID = 4200786738903617164L;
	
	private ControlFlowEdge cfgEdge;
	
	public CCFGCodeEdge(ControlFlowEdge cfgEdge) {
		this.cfgEdge = cfgEdge;
	}

	public ControlFlowEdge getCfgEdge() {
		return cfgEdge;
	}

	@Override
	public String toString() {
		return cfgEdge.toString();
	}
}
