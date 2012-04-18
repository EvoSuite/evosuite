package de.unisb.cs.st.evosuite.graphs.ccfg;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;

public class CCFGMethodExitNode extends CCFGNode {

	private String method;
	private Set<Map<String, BytecodeInstruction>> activeDefs;

	public CCFGMethodExitNode(String method) {
		this.method = method;
	}

	public boolean isExitOfMethodEntry(CCFGMethodEntryNode methodEntry) {
		if (methodEntry == null)
			return false;
		return methodEntry.getMethod().equals(method);
	}

	public String getMethod() {
		return method;
	}

	public void addActiveDefs(Map<String, BytecodeInstruction> activeDefs) {
		if (this.activeDefs == null)
			this.activeDefs = new HashSet<Map<String, BytecodeInstruction>>();
		this.activeDefs.add(activeDefs);
	}

	public Set<Map<String, BytecodeInstruction>> getActiveDefs() {
		return activeDefs;
	}
	
	public void forgetActiveDefs() {
		activeDefs = null;
	}

	@Override
	public String toString() {
		return "Exit: " + method;
	}
}
