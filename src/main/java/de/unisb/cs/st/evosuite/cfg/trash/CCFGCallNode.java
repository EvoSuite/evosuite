package de.unisb.cs.st.evosuite.cfg.trash;

import de.unisb.cs.st.evosuite.graphs.ccfg.CCFGNode;
import de.unisb.cs.st.evosuite.graphs.ccg.ClassCallEdge;

public class CCFGCallNode extends CCFGNode {

	private ClassCallEdge correspondingCall;
	
	public CCFGCallNode(ClassCallEdge correspondingCall) {
		this.correspondingCall = correspondingCall;
	}
	
	@Override
	public String toString() {
		return "CCFG_MethodCall_"+correspondingCall.toString();
	}
	
}
