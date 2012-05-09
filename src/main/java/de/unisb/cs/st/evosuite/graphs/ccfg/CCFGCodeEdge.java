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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cfgEdge == null) ? 0 : cfgEdge.hashCode());
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
		CCFGCodeEdge other = (CCFGCodeEdge) obj;
		if (cfgEdge == null) {
			if (other.cfgEdge != null)
				return false;
		} else if (!cfgEdge.equals(other.cfgEdge))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return cfgEdge.toString();
	}
}
