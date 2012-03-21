package de.unisb.cs.st.evosuite.coverage.behavioral;

/** Class that represents a node caused by an exception. */
public class BCExceptionNode extends BCNode {
	
	/** The name of the node. */
	private String name = "ExceptionNode";
	
	/**
	 * Creates a new exception node with default name
	 * and object state <tt>null</tt>.
	 */
	public BCExceptionNode() {
		super(null);
	}
	
	/**
	 * Creates a new exception node with given name.
	 * The object state of this node will be set
	 * to <tt>null</tt>.</p>
	 * 
	 * @param exceptionName - the name of the exception for this node. 
	 */
	public BCExceptionNode(String exceptionName) {
		super(null);
		
		if (exceptionName != null)
			name = exceptionName;
	}
	
	/**
	 * Two exception nodes are equal if they have the same name.</p>
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BCExceptionNode) {
			BCExceptionNode node = (BCExceptionNode) obj;
			return name.equals(node.getName());
		}
		return false;
	}
	
	/**
	 * Returns a hash code for this exception node,
	 * i.e. the hash code of the name of this node.</p>
	 * 
	 * @return a hash code for this exception node.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	/**
	 * Returns a string representation this node,
	 * i.e. the name of this exception node.</p>
	 * 
	 * @return a string representation of this node.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Returns the name of this node.</p>
	 * 
	 * @return the name of this node.
	 */
	public String getName() {
		return name;
	}
}
