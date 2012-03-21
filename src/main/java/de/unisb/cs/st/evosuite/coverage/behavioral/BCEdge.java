package de.unisb.cs.st.evosuite.coverage.behavioral;

import de.unisb.cs.st.evosuite.testcase.TestCase;

/** Class that represents a concrete edge. */
public class BCEdge {
	
	/** The node the edge leads away. */
	private BCNode startNode;
	
	/** The test-case representing the transition call. */
	private TestCase transition;
	
	/** The node the edge leads to. */
	private BCNode endNode;
	
	/** The transition sequence leading to the edge. */
	private TransitionSequence alpha = new TransitionSequence();
	
	/** A flag whether the edge is the last edge in alpha. */
	private boolean lastInAlpha = false;
	
	/** A flag whether the edge was discovered. */
	private boolean discovered = false;
	
	/**
	 * Creates a new edge with given start node, transition
	 * and end node.
	 * 
	 * <p><b>Note:</b> As side effects this constructor adds
	 * the created edge to the start node's set of outgoing edges
	 * and to the end node's set of incoming edges.</p>
	 * 
	 * @param startNode - the start node of this edge.
	 * @param transition - the method call represented by this edge given as a test-case.
	 * @param endNode - the end node of this edge.
	 * 
	 * @throws IllegalArgumentException
	 *             if one of the parameters is <tt>null</tt>.
	 */
	public BCEdge(BCNode startNode, TestCase transition, BCNode endNode) {
		if (startNode == null)
			throw new IllegalArgumentException("The given start node of the edge is null!");
		if (transition == null)
			throw new IllegalArgumentException("The given transition of the edge is null!");
		if (endNode == null)
			throw new IllegalArgumentException("The given end node of the edge is null!");
		
		this.startNode = startNode;
		this.transition = transition;
		this.endNode = endNode;
		
		// add this edge to the corresponding sets in the nodes
		startNode.addOutgoingEdge(this);
		endNode.addIncomingEdge(this);
	}
	
	/**
	 * Two edges are equal if they have the same
	 * start node, transition and end node.</p>
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BCEdge) {
			BCEdge edge = (BCEdge) obj;
			return startNode.equals(edge.getStartNode())
						&& transition.equals(edge.getTransition())
								&& endNode.equals(edge.getEndNode());
		}
		return false;
	}
	
	/**
	 * Returns a hash code for this edge, i.e. the sum
	 * of hash codes of the start node, the transition
	 * and end node.</p>
	 * 
	 * @return a hash code for this edge.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return startNode.hashCode() + transition.hashCode() + endNode.hashCode();
	}
	
	/**
	 * Returns a string representation of this edge,
	 * i.e. a triple (s,t,e) where</br>
	 * 		s - is the string representation of the start node</br>
	 * 		t - is the transition invoking method call</br>
	 * 		e - is the string representation of the end node.</p>
	 * 
	 * @return a string representation of this edge.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer("(");
		result.append(startNode.toString()).append(",");
		result.append(MethodCallHandler.getSignature(transition)).append(",");
		result.append(endNode.toString()).append(")");
		return result.toString();
	}
	
	/**
	 * Returns the start node of this edge.</p>
	 * 
	 * @return the start node of this edge.
	 */
	public BCNode getStartNode() {
		return startNode;
	}
	
	/**
	 * Returns the transition of this edge.</p>
	 * 
	 * @return the transition of this edge.
	 */
	public TestCase getTransition() {
		return transition;
	}
	
	/**
	 * Returns the end node of this edge.</p>
	 * 
	 * @return the end node of this edge.
	 */
	public BCNode getEndNode() {
		return endNode;
	}
	
	/**
	 * Returns the transition sequence leading to this edge.</p>
	 * 
	 * @return the transition sequence leading to this edge.
	 */
	public TransitionSequence getTransitionSequence() {
		return alpha;
	}
	
	/**
	 * Sets the transition sequence of this edge,
	 * i.e. a new transition sequence with given alpha
	 * or an empty one if alpha is <tt>null</tt>.</p>
	 * 
	 * @param alpha - the transition sequence to set.
	 * @param isLastInAlpha - a flag whether this edge is the last one in alpha.
	 */
	public void setTransitionSequence(TransitionSequence alpha, boolean isLastInAlpha) {
		this.alpha = new TransitionSequence(alpha);
		this.lastInAlpha = isLastInAlpha;
	}
	
	/**
	 * Indicates whether this edge is the last edge in alpha.</p>
	 * 
	 * @return <tt>true</tt> if this edge is the last one in alpha; <tt>false</tt> otherwise.
	 */
	public boolean isLastInAlpha() {
		return lastInAlpha;
	}
	
	/**
	 * Sets whether this edge is the last edge in alpha.</p>
	 * 
	 * @param isLastInAlpha - a flag whether this edge is the last one in alpha.
	 */
	public void setLastInAlpha(boolean isLastInAlpha) {
		lastInAlpha = isLastInAlpha;
	}
	
	/**
	 * Indicates whether this edge was discovered.</p>
	 * 
	 * @return <tt>true</tt> if this edge was discovered; <tt>false</tt> otherwise.
	 */
	public boolean isDiscovered() {
		return discovered;
	}
	
	/**
	 * Sets whether this edge was discovered.</p>
	 * 
	 * @param aFlag - a flag whether this edge was discovered.
	 */
	public void setDiscovered(boolean aFlag) {
		discovered = aFlag;
	}
}
