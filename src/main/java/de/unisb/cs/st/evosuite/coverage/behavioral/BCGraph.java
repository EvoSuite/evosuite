package de.unisb.cs.st.evosuite.coverage.behavioral;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import de.unisaarland.cs.st.adabu.trans.model.MethodInvocation;
import de.unisaarland.cs.st.adabu.trans.model.MethodInvocationSet;
import de.unisaarland.cs.st.adabu.trans.model.TransitiveObjectModel;
import de.unisaarland.cs.st.adabu.trans.model.TransitiveObjectState;
import de.unisaarland.cs.st.adabu.util.datastructures.GraphEdge;
import de.unisaarland.cs.st.adabu.util.datastructures.GraphNode;
import de.unisb.cs.st.evosuite.testcase.TestCase;

/** Class that provides a simple graph representation of an object behavior model. */
public class BCGraph {
	
	/** The start node of the graph. */
	private BCNode startNode;
	
	/** The set containing all nodes of the graph. */
	private Set<BCNode> nodes;
	
	/** The set containing all edges of the graph. */
	private Set<BCEdge> edges;
	
	/**
	 * Creates a new graph with given start node, set of all nodes
	 * and set of all edges.
	 * 
	 * <p><b>Note:</b> This constructor does <b>not</b> check whether
	 * the underlying graph is consistent.</p>
	 * 
	 * @param startNode - the root node of this graph.
	 * @param nodes - the nodes of this graph.
	 * @param edges - the edges of this graph.
	 * 
	 * @throws IllegalArgumentException
	 *             if one of the parameters is <tt>null</tt>.
	 */
	public BCGraph(BCEmptyNode startNode, Set<BCNode> nodes, Set<BCEdge> edges) {
		if (startNode == null)
			throw new IllegalArgumentException("The given start node of the graph is null!");
		if (nodes == null)
			throw new IllegalArgumentException("The given set of nodes of the graph is null!");
		if (edges == null)
			throw new IllegalArgumentException("The given set of edges of the graph is null!");
		
		this.startNode = startNode;
		this.nodes = nodes;
		this.edges = edges;
	}
	
	/**
	 * Creates a new graph with given object behavior model
	 * and method call handler.</p>
	 * 
	 * @param objectModel - the object model holding the data for this graph.
	 * @param handler - the handler holding the edge data for this graph.
	 * 
	 * @throws IllegalArgumentException
	 *             if one of the parameters is <tt>null</tt>.
	 */
	public BCGraph(TransitiveObjectModel objectModel, MethodCallHandler handler) {
		if (objectModel == null)
			throw new IllegalArgumentException("The given object model is null!");
		if (handler == null)
			throw new IllegalArgumentException("The given method call handler is null!");
		
		startNode = new BCEmptyNode();
		nodes = new HashSet<BCNode>();
		edges = new HashSet<BCEdge>();
		Vector<GraphNode<TransitiveObjectState,MethodInvocationSet>> oldNodes;
		Vector<GraphEdge<TransitiveObjectState,MethodInvocationSet>> oldEdges;
		Map<GraphNode<TransitiveObjectState,MethodInvocationSet>,BCNode> oldToNewNode;
		
		oldNodes = objectModel.getModel().getNodes();
		oldEdges = objectModel.getModel().getEdges();
		oldToNewNode = new HashMap<GraphNode<TransitiveObjectState,MethodInvocationSet>,BCNode>();
		
		// create an exception node
		BCNode exNode = new BCExceptionNode();
		
		// create the new nodes and edges
		int nodeIDCounter = 1;
		switch (oldNodes.size()) {
		// case number of nodes equal 0
		case 0:
		{
			// graph only contains the empty start node
			nodes.add(startNode);
			return;
		}
		
		// case number of nodes equal 1
		case 1:
		{
			// model consists of one node with empty data
			GraphNode<TransitiveObjectState,MethodInvocationSet> oldNode = oldNodes.firstElement();
			
			// add the start node
			nodes.add(startNode);
			
			// create a new node with the old nodes empty data
			BCNode newNode = new BCIdNode(nodeIDCounter++, oldNode.getData());
			nodes.add(newNode);
			
			// create the new edges
			for (GraphEdge<TransitiveObjectState,MethodInvocationSet> oldEdge : oldEdges) {
				BCNode start;
				BCNode end;
				TestCase transition;
				
				// create a new edge for every method invocation
				for (MethodInvocation invocation : oldEdge.getData()) {
					// get test-case from handler
					transition = handler.getTestCase(oldEdge.getStartNode().getData(), invocation);
					
					// check whether the transition is valid
					if (transition == null) {
						System.out.println("* Warning: Couldn't create the edge for the invocation: " + invocation);
						System.out.println("in state: " + oldEdge.getStartNode().getData());
						continue;
					}
					
					// check whether the method invocation is a constructor call
					if (invocation.getIdentifier().isConstructor()) {
						start = startNode;
						end = newNode;
					} else {
						start = newNode;
						end = newNode;
					}
					
					// check whether the invocation raised an exception
					if (invocation.exceptionRaised()) {
						end = exNode;
						nodes.add(exNode); // add the exception node to the set of nodes
					}
					
					edges.add(new BCEdge(start, transition, end));
				}
			}
			
			break;
		}
		
		// case number of nodes greater than 1
		default:
		{
			// create the new nodes with unique id
			for (GraphNode<TransitiveObjectState,MethodInvocationSet> oldNode : oldNodes) {
				BCNode newNode;
				
				// node with no incoming edges is the start
				if (oldNode.getIncomingEdges().isEmpty()) {
					newNode = startNode;
				} else {
					newNode = new BCIdNode(nodeIDCounter++, oldNode.getData());
				}
				
				nodes.add(newNode);
				oldToNewNode.put(oldNode, newNode);
			}
			
			// create the new edges
			for (GraphEdge<TransitiveObjectState,MethodInvocationSet> oldEdge : oldEdges) {
				BCNode start = oldToNewNode.get(oldEdge.getStartNode());
				BCNode end;
				TestCase transition;
				
				// create a new edge for every method invocation
				for (MethodInvocation invocation : oldEdge.getData()) {
					// get test-case from handler
					transition = handler.getTestCase(oldEdge.getStartNode().getData(), invocation);
					
					// check whether the transition is valid
					if (transition == null) {
						System.out.println("* Warning: Couldn't create the edge for the invocation: " + invocation);
						System.out.println("in state: " + oldEdge.getStartNode().getData());
						continue;
					}
					
					// check whether the invocation raised an exception
					if (invocation.exceptionRaised()) {
						end = exNode;
						nodes.add(exNode); // add the exception node to the set of nodes
					} else {
						end = oldToNewNode.get(oldEdge.getEndNode());
					}
					
					edges.add(new BCEdge(start, transition, end));
				}
			}
		}
		}
	}
	
	/**
	 * Two graphs are equal if they have the same
	 * nodes and edges.</p>
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BCGraph) {
			BCGraph graph = (BCGraph) obj;
			return startNode.equals(graph.getStartNode())
						&& nodes.equals(graph.getNodes())
								&& edges.equals(graph.getEdges());
		}
		return false;
	}
	
	/**
	 * Returns a hash code for this graph, i.e. the sum
	 * of hash codes of the nodes multiplied by the sum of
	 * hash codes of the edges of this graph.</p>
	 * 
	 * @return a hash code for this graph.
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return nodes.hashCode() * edges.hashCode();
	}
	
	/**
	 * Returns a string representation of this graph.
	 * The graph is described via <tt>DOT</tt>.</p>
	 * 
	 * @return a string representation of this graph.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("digraph automaton {\n");
		for (BCEdge edge : edges) {
			result.append("\t" + edge.getStartNode() + " -> " + edge.getEndNode());
			result.append(" [label=\"" + MethodCallHandler.getSignature(edge.getTransition()) + "\"];\n");
		}
		result.append("}\n");
		return result.toString();
	}
	
	/**
	 * Returns a set of nodes containing all end nodes of this graph, where
	 * an end node is a node with no outgoing edges.</p>
	 * 
	 * @return a set of end nodes of this graph.
	 */
	public Set<BCNode> getEndNodes() {
		Set<BCNode> endNodes = new HashSet<BCNode>();
		for (BCNode node : nodes) {
			if (node.getOutgoingEdges().isEmpty())
				endNodes.add(node);
		}
		return endNodes;
	}
	
	/**
	 * Returns the start node of this graph.</p>
	 * 
	 * @return the start node of this graph.
	 */
	public BCNode getStartNode() {
		return startNode;
	}
	
	/**
	 * Returns the set of nodes of this graph.</p>
	 * 
	 * @return the set of nodes of this graph.
	 */
	public Set<BCNode> getNodes() {
		return nodes;
	}
	
	/**
	 * Returns the set of edges of this graph.</p>
	 * 
	 * @return the set of edges of this graph.
	 */
	public Set<BCEdge> getEdges() {
		return edges;
	}
}
