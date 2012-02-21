package de.unisb.cs.st.evosuite.cfg.trash;

import de.unisb.cs.st.evosuite.graphs.ccfg.CCFGNode;
import de.unisb.cs.st.evosuite.graphs.ccg.ClassCallNode;

public class CCFGTempNode extends CCFGNode {

	private ClassCallNode methodNode;

	public CCFGTempNode(ClassCallNode methodNode) {
		this.methodNode = methodNode;
	}

	public ClassCallNode getMethodNode() {
		return methodNode;
	}	
}
