package de.unisb.cs.st.evosuite.cfg.trash;

import de.unisb.cs.st.evosuite.graphs.ccfg.CCFGEdge;
import de.unisb.cs.st.evosuite.graphs.ccg.ClassCallEdge;

public class CCFGClassCallEdge extends CCFGEdge {

	private static final long serialVersionUID = -1638791707105165885L;

	private ClassCallEdge callEdge;
	
	public ClassCallEdge getCallEdge() {
		return callEdge;
	}

	public CCFGClassCallEdge(ClassCallEdge callEdge) {
		this.callEdge = callEdge;
	}
}
