package de.unisb.cs.st.evosuite.coverage.behavioral;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.unisaarland.cs.st.adabu.trans.model.TransitiveObjectState;

/** Abstract class that represents a concrete node. */
public abstract class BCNode {
	
	/** The set of incoming edges of the node. */
	protected Set<BCEdge> incomingEdges = new HashSet<BCEdge>();
	
	/** The set of outgoing edges of the node. */
	protected Set<BCEdge> outgoingEdges = new HashSet<BCEdge>();
	
	/** The object state of the node. */
	protected TransitiveObjectState objectState;
	
	/**
	 * Creates a new node with given object state.</p>
	 * 
	 * @param objectState - the object state for this node.
	 */
	protected BCNode(TransitiveObjectState objectState) {
		this.objectState = objectState;
	}
	
	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract int hashCode();
	
	@Override
	public abstract String toString();
	
	/**
	 * Creates a new list containing all incoming edges of
	 * this node that are not yet marked as discovered.</p>
	 * 
	 * @return a list of all incoming edges of this node
	 *         that are not marked as discovered.
	 */
	public List<BCEdge> getUndiscoveredIncomingEdges() {
		ArrayList<BCEdge> undiscoveredIncomingEdges = new ArrayList<BCEdge>(incomingEdges.size());
		for (BCEdge edge : incomingEdges) {
			if (!edge.isDiscovered())
				undiscoveredIncomingEdges.add(edge);
		}
		undiscoveredIncomingEdges.trimToSize();
		return undiscoveredIncomingEdges;
	}
	
	/**
	 * Creates a new list containing all outgoing edges of
	 * this node that are not yet marked as discovered.</p>
	 * 
	 * @return a list of all outgoing edges of this node
	 *         that are not marked as discovered.
	 */
	public List<BCEdge> getUndiscoveredOutgoingEdges() {
		ArrayList<BCEdge> undiscoveredOutgoingEdges = new ArrayList<BCEdge>(outgoingEdges.size());
		for (BCEdge edge : outgoingEdges) {
			if (!edge.isDiscovered())
				undiscoveredOutgoingEdges.add(edge);
		}
		undiscoveredOutgoingEdges.trimToSize();
		return undiscoveredOutgoingEdges;
	}
	
	/**
	 * Adds a given edge to the set of incoming edges of this node.</p>
	 * 
	 * @param edge - the edge to be added.
	 */
	public void addIncomingEdge(BCEdge edge) {
		assert (edge != null);
		incomingEdges.add(edge);
	}
	
	/**
	 * Adds a given edge to the set of outgoing edges of this node.</p>
	 * 
	 * @param edge - the edge to be added.
	 */
	public void addOutgoingEdge(BCEdge edge) {
		assert (edge != null);
		outgoingEdges.add(edge);
	}
	
	/**
	 * Removes a given edge from the set of incoming edges of this node.</p>
	 * 
	 * @param edge - the edge to be removed.
	 */
	public void removeIncomingEdge(BCEdge edge) {
		assert (edge != null);
		incomingEdges.remove(edge);
	}
	
	/**
	 * Removes a given edge from the set of outgoing edges of this node.</p>
	 * 
	 * @param edge - the edge to be removed.
	 */
	public void removeOutgoingEdge(BCEdge edge) {
		assert (edge != null);
		outgoingEdges.remove(edge);
	}
	
	/**
	 * Returns the set of incoming edges of this node.</p>
	 * 
	 * @return the set of incoming edges of this node.
	 */
	public Set<BCEdge> getIncomingEdges() {
		return incomingEdges;
	}
	
	/**
	 * Returns the set of outgoing edges of this node.</p>
	 * 
	 * @return the set of outgoing edges of this node.
	 */
	public Set<BCEdge> getOutgoingEdges() {
		return outgoingEdges;
	}
	
	/**
	 * Returns the object state of this node.</p>
	 * 
	 * @return the object state of this node.
	 */
	public TransitiveObjectState getObjectState() {
		return objectState;
	}
}
