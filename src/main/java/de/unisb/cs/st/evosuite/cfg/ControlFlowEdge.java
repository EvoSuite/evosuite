package de.unisb.cs.st.evosuite.cfg;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

/**
 * Is used insides CFGs to represent edges.
 * 
 *  Nothing more then an DefaultEdge that holds it's own references to
 *  it's source and target Node
 *  
 *  TODO remove this class again!
 *  		... there seems to be a reason why jGraph's edges don't do that already
 *  		... learned stuff about generics and type erasure in java ... do not like
 *  
 * 
 * @author Andre Mis
 */
public class ControlFlowEdge extends DefaultEdge {

	private static final long serialVersionUID = 1640580801274061062L;

	private static int edgeCount = 0;
	
	private int id;
	
	private BasicBlock src;
	private BasicBlock target;
	
	public ControlFlowEdge(BasicBlock src, BasicBlock target) {
		if (src == null || target == null)
			throw new IllegalArgumentException("null given");
		
		this.src = src;
		this.target = target;
		setId();
	}
	
	private void setId() {
		edgeCount++;
		this.id = edgeCount;
	}
	
	public BasicBlock getSource() {
		return src;
	}
	
	public BasicBlock getTarget() {
		return target;
	}
	
	@Override
	public String toString() {
		return "ControlFlowEdge_" + id + " from " + src.getName() + " to "
				+ target.getName();
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
