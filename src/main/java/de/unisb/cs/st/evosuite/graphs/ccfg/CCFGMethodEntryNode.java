package de.unisb.cs.st.evosuite.graphs.ccfg;

public class CCFGMethodEntryNode extends CCFGNode {

	private String method;
	private CCFGCodeNode entryInstruction;
	
	public CCFGMethodEntryNode(String method, CCFGCodeNode entryInstruction) {
		this.method = method;
		this.entryInstruction = entryInstruction;
	}
	
	public String getMethod() {
		return method;
	}

	public CCFGCodeNode getEntryInstruction() {
		return entryInstruction;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((entryInstruction == null) ? 0 : entryInstruction.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CCFGMethodEntryNode other = (CCFGMethodEntryNode) obj;
		if (entryInstruction == null) {
			if (other.entryInstruction != null)
				return false;
		} else if (!entryInstruction.equals(other.entryInstruction))
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Entry: "+method;
	}
}
