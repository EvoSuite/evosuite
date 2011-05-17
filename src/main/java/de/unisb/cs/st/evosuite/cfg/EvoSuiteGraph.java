package de.unisb.cs.st.evosuite.cfg;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 * Supposed to become the super class of all kinds of graphs used within
 * EvoSuite Examples are the raw and minimal Control Flow Graph and hopefully at
 * one point the Control Dependency Tree
 * 
 * This class is supposed to hide the jGraph library from the rest of EvoSuite
 * and is supposed to serve as an interface for all kinds of primitive graph-
 * functionality such as asking for information about the nodes and edges of the
 * graph and the relations between them.
 * 
 * Hopefully at some point only this class and it's sub classes are the only
 * files in EvoSuite that import anything from the jGraph library - at least
 * that's the idea This is very similar to the way cfg.ASMWrapper is supposed to
 * hide the ASM library and serve as an interface for BytecodeInstrucions
 * 
 * So most of this class' methods are just wrappers that redirect the specific
 * call to the corresponding jGraph-method
 * 
 * For now an EvoSuiteGraph can be any DirectedGraph, depending on
 * 
 * @author Andre Mis
 */
public abstract class EvoSuiteGraph<V, E extends DefaultEdge> {

	private DirectedGraph<V, E> graph;

	protected EvoSuiteGraph(Class<E> cl) {

		graph = new DefaultDirectedGraph<V, E>(cl);
	}

	protected EvoSuiteGraph(DirectedGraph<V, E> graph) {
		if (graph == null)
			throw new IllegalArgumentException("null given");

		this.graph = graph;
	}
	
	
	// TODO: this is supposed to be removed in the future!
	// 			only supposed to be used for refactoring the cfg package
	//			DO NOT CALL THIS! please ;)
	public DirectedGraph<V,E> getGraph() {
		return graph;
	}

	// retrieving nodes and edges

	public V getEdgeSource(E e) {
		if (!containsEdge(e))
			throw new IllegalArgumentException("edge not in graph");

		return graph.getEdgeSource(e);
	}

	public V getEdgeTarget(E e) {
		if (!containsEdge(e))
			throw new IllegalArgumentException("edge not in graph");

		return graph.getEdgeTarget(e);
	}
	
	public Set<E> outgoingEdgesOf(V node) {
		if (!containsBlock(node)) // should this just return null?
			throw new IllegalArgumentException(
					"block not contained in this CFG");

		return graph.outgoingEdgesOf(node);
	}

	public Set<E> incomingEdgesOf(V node) {
		if (!containsBlock(node)) // should this just return null?
			throw new IllegalArgumentException(
					"block not contained in this CFG");

		return graph.incomingEdgesOf(node);
	}
	
	public Set<V> getChildren(V node) {
		if (!containsBlock(node)) // should this just return null?
			throw new IllegalArgumentException(
					"block not contained in this CFG");

		Set<V> r = new HashSet<V>();
		for (E e : outgoingEdgesOf(node))
			r.add(getEdgeTarget(e));

		// sanity check
		if (r.size() != outDegreeOf(node))
			throw new IllegalStateException(
					"expect children count and size of set of all children of a CFGs node to be equals");

		return r;
	}

	public Set<V> getParents(V node) {
		if (!containsBlock(node)) // should this just return null?
			throw new IllegalArgumentException(
					"block not contained in this CFG");

		Set<V> r = new HashSet<V>();
		for (E e : outgoingEdgesOf(node))
			r.add(getEdgeTarget(e));

		// sanity check
		if (r.size() != outDegreeOf(node))
			throw new IllegalStateException(
					"expect children count and size of set of all children of a CFGs node to be equals");

		return r;
	}
	
	protected Set<V> vertexSet() {
		return graph.vertexSet();
	}

	protected Set<E> edgeSet() {
		return graph.edgeSet();
	}
	
	// building the graph
	
	protected boolean addVertex(V v) {
		return graph.addVertex(v);
	}

	protected E addEdge(V src, V target) {
		
		return graph.addEdge(src,target);
	}
	
	protected boolean addEdge(V src, V target, E e) {
		
		return graph.addEdge(src, target, e);
	}

	// different counts

	public int getNodeCount() {
		return graph.vertexSet().size();
	}

	public int getEdgeCount() {
		return graph.edgeSet().size();
	}
	
	public int outDegreeOf(V node) { // TODO rename to sth. like childCount()
		if (node == null || !containsBlock(node))
			return -1;

		return graph.outDegreeOf(node);
	}
	
	public int inDegreeOf(V node) { // TODO rename sth. like parentCount()
		if (node == null || !containsBlock(node))
			return -1;

		return graph.inDegreeOf(node);
	}

	// some queries

	public boolean containsBlock(V block) {
		// documentation says containsVertex() returns false on when given null
		return graph.containsVertex(block);
	}

	public boolean containsEdge(E e) {
		return graph.containsEdge(e);
	}

	public boolean isEmpty() {
		return graph.vertexSet().isEmpty();
	}
	
	public boolean hasNPartentsMChildren(V node, int parents, int children) {
		if (node == null || !containsBlock(node))
			return false;

		return inDegreeOf(node) == parents
				&& outDegreeOf(node) == children;
	}

}
