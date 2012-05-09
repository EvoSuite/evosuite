package de.unisb.cs.st.evosuite.graphs.ccg;

import de.unisb.cs.st.evosuite.graphs.ccfg.CCFGNode;


public class ClassCallNode extends CCFGNode {

	private String method;
	
	public String getMethod() {
		return method;
	}

	public ClassCallNode(String method) {
		this.method = method;
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
		ClassCallNode other = (ClassCallNode) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return method;
	}

}
