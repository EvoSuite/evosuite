package de.unisb.cs.st.evosuite.graphs.ccfg;

public class CCFGMethodExitNode extends CCFGNode {

	private String method;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		CCFGMethodExitNode other = (CCFGMethodExitNode) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Exit: " + method;
	}
}
