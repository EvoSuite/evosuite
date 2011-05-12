package de.unisb.cs.st.evosuite.cfg;

import org.jgrapht.graph.DefaultEdge;

public class ControlFlowEdge extends DefaultEdge {

	private static final long serialVersionUID = -5009449930477928101L;

	
	private BasicBlock src;
	private BasicBlock target;
	
	public ControlFlowEdge(BasicBlock src, BasicBlock target) {
		if (src == null || target == null)
			throw new IllegalArgumentException("null given");
		
		this.src = src;
		this.target = target;
	}
	
	public BasicBlock getSource() {
		return src;
	}
	
	public BasicBlock getTarget() {
		return target;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ControlFlowEdge))
			return false;

		ControlFlowEdge other = (ControlFlowEdge) obj;

		return src.equals(other.src) && target.equals(other.target);
	}

}
