package de.unisb.cs.st.evosuite.coverage.behavioral;

import de.unisaarland.cs.st.adabu.trans.model.TransitiveObjectState;

/** Class that represents an uninitialized node. */
public class BCEmptyNode extends BCNode {
	
	/**
	 * Creates a new empty node with empty object state.
	 */
	public BCEmptyNode() {
		super(new TransitiveObjectState(0));
	}
	
	/**
	 * Two empty nodes are equal.</p>
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof BCEmptyNode;
	}
	
	/**
	 * Returns a hash code for this empty node,
	 * i.e. '7'.</p>
	 * 
	 * @return a hash code for this empty node.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 7;
	}
	
	/**
	 * Returns a string representation of this empty node,
	 * i.e. 'E'.</p>
	 * 
	 * @return a string representation of this node.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "E";
	}
}
