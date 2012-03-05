package de.unisb.cs.st.evosuite.graphs.ccg;

import de.unisb.cs.st.evosuite.graphs.GraphPool;
import de.unisb.cs.st.evosuite.graphs.ccfg.CCFGNode;
import de.unisb.cs.st.evosuite.graphs.cfg.RawControlFlowGraph;


public class ClassCallNode extends CCFGNode {

	private String method;
	
	public String getMethod() {
		return method;
	}

	public ClassCallNode(String method) {
		this.method = method;
	}
	
	@Override
	public String toString() {
		return method;
	}

}
