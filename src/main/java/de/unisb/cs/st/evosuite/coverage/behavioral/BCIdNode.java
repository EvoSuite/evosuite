package de.unisb.cs.st.evosuite.coverage.behavioral;

import de.unisaarland.cs.st.adabu.trans.model.TransitiveObjectState;

/** Class that represents a normal node. */
public class BCIdNode extends BCNode {
	
	/** The unique id of the node. */
	private int id;
	
	/**
	 * Creates a new node with given id and object state.</p>
	 * 
	 * @param id - the unique id of this node.
	 * @param objectState - the object state of this node. 
	 */
	public BCIdNode(int id, TransitiveObjectState objectState) {
		super(objectState);
		this.id = id;
	}
	
	/**
	 * Two nodes are equal if they have the same id.</p>
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BCIdNode) {
			return ((BCIdNode) obj).getId() == id;
		}
		return false;
	}
	
	/**
	 * Returns a hash code for this node,
	 * i.e. the id of this node.</p>
	 * 
	 * @return a hash code for this node.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return id;
	}
	
	/**
	 * Returns a string representation this node.</p>
	 * 
	 * @return a string representation of this node.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Node" + id;
	}
	
	/**
	 * Returns the unique id of this node.</p>
	 * 
	 * @return the unique id of this node.
	 */
	public int getId() {
		return id;
	}
}
