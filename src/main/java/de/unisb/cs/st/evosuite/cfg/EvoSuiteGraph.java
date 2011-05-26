package de.unisb.cs.st.evosuite.cfg;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;


// TODO visualization using .DOT

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
 * For now an EvoSuiteGraph can always be represented by a DefaultDirectedGraph
 * from the jGraph library - that is a directed graph not allowed to contain
 * multiple edges between to nodes but allowed to contain cycles
 * 
 * 
 * @author Andre Mis
 */
public abstract class EvoSuiteGraph<V> {

	private static Logger logger = Logger.getLogger(DominatorTree.class);
	
	
	protected DefaultDirectedGraph<V, DefaultEdge> graph;

	
	protected EvoSuiteGraph() {

		graph = new DefaultDirectedGraph<V, DefaultEdge>(DefaultEdge.class);
	}

	protected EvoSuiteGraph(DefaultDirectedGraph<V, DefaultEdge> graph) {
		if (graph == null)
			throw new IllegalArgumentException("null given");

		this.graph = graph;
	}
	
	// retrieving nodes and edges

	public V getEdgeSource(DefaultEdge e) {
		if (!containsEdge(e))
			throw new IllegalArgumentException("edge not in graph");

		return graph.getEdgeSource(e);
	}

	public V getEdgeTarget(DefaultEdge e) {
		if (!containsEdge(e))
			throw new IllegalArgumentException("edge not in graph");

		return graph.getEdgeTarget(e);
	}
	
	public Set<DefaultEdge> outgoingEdgesOf(V node) {
		if (!containsVertex(node)) // should this just return null?
			throw new IllegalArgumentException(
					"block not contained in this CFG");
		// TODO copy set
		return graph.outgoingEdgesOf(node);
	}

	public Set<DefaultEdge> incomingEdgesOf(V node) {
		if (!containsVertex(node)) // should this just return null?
			throw new IllegalArgumentException(
					"block not contained in this CFG");
		// TODO copy set
		return graph.incomingEdgesOf(node);
	}
	
	public Set<V> getChildren(V node) {
		if (!containsVertex(node)) // should this just return null?
			throw new IllegalArgumentException(
					"block not contained in this CFG");

		Set<V> r = new HashSet<V>();
		for (DefaultEdge e : outgoingEdgesOf(node))
			r.add(getEdgeTarget(e));

		// sanity check
		if (r.size() != outDegreeOf(node))
			throw new IllegalStateException(
					"expect children count and size of set of all children of a CFGs node to be equals");

		return r;
	}

	public Set<V> getParents(V node) {
		if (!containsVertex(node)) // should this just return null?
			throw new IllegalArgumentException(
					"block not contained in this CFG");

		Set<V> r = new HashSet<V>();
		for (DefaultEdge e : incomingEdgesOf(node))
			r.add(getEdgeSource(e));

		// sanity check
		if (r.size() != inDegreeOf(node))
			throw new IllegalStateException(
					"expect parent count and size of set of all parents of a CFGs node to be equals");

		return r;
	}
	
	// TODO make SetUtils.copySet() or something for the following and other similar methods
	
	public Set<V> vertexSet() {
		Set<V> r = new HashSet<V>();
		
		for(V v : graph.vertexSet())
			r.add(v);
		
		return r;
	}

	protected Set<DefaultEdge> edgeSet() {
		Set<DefaultEdge> r = new HashSet<DefaultEdge>();
		
		for(DefaultEdge e : graph.edgeSet())
			r.add(e);
		
		return r;
	}
	
	// building the graph
	
	protected void addVertices(Collection<V> vs) {
		if(vs==null)
			throw new IllegalArgumentException("null given");
		for(V v : vs)
			if(!addVertex(v))
				throw new IllegalArgumentException("unable to add all nodes in given collection: "+v.toString());
			
	}
	
	protected boolean addVertex(V v) {
		return graph.addVertex(v);
	}

	protected DefaultEdge addEdge(V src, V target) {
		
		return graph.addEdge(src,target);
	}
	
	protected boolean addEdge(V src, V target, DefaultEdge e) {
		
		return graph.addEdge(src, target, e);
	}

	// different counts

	public int vertexCount() {
		return graph.vertexSet().size();
	}

	public int edgeCount() {
		return graph.edgeSet().size();
	}
	
	public int outDegreeOf(V node) { // TODO rename to sth. like childCount()
		if (node == null || !containsVertex(node))
			return -1;

		return graph.outDegreeOf(node);
	}
	
	public int inDegreeOf(V node) { // TODO rename sth. like parentCount()
		if (node == null || !containsVertex(node))
			return -1;

		return graph.inDegreeOf(node);
	}

	// some queries

	public DefaultEdge getEdge(V v1, V v2) {
		return graph.getEdge(v1, v2);
	}
	
	public boolean containsVertex(V v) {
		// documentation says containsVertex() returns false on when given null
		return graph.containsVertex(v);
	}

	public boolean containsEdge(V v1, V v2) {
		return graph.containsEdge(v1,v2);
	}
	
	public boolean containsEdge(DefaultEdge e) {
		return graph.containsEdge(e); // TODO this seems to be buggy, at least for ControlFlowEdges
	}

	public boolean isEmpty() {
		return graph.vertexSet().isEmpty();
	}
	
	/**
	 * Checks whether each vertex inside this graph is reachable
	 * from some other vertex   
	 */
	public boolean isConnected() {
		if (vertexCount() < 2)
			return true;

		V start = getRandomVertex();
		Set<V> connectedToStart = determineConnectedVertices(start);

		return connectedToStart.size() == vertexSet().size();
	}

	/**
	 * Follows all edges adjacent to the given vertex v ignoring edge directions
	 * and returns a set containing all vertices visited that way
	 */
	public Set<V> determineConnectedVertices(V v) {
		
		Set<V> visited = new HashSet<V>();
		Queue<V> queue = new LinkedList<V>();
		
		queue.add(v);
		while (!queue.isEmpty()) {
			V current = queue.poll();
			if (visited.contains(current))
				continue;
			visited.add(current);
			
			queue.addAll(getParents(current));
			queue.addAll(getChildren(current));
		}
		
		return visited;
	}

	public boolean hasNPartentsMChildren(V node, int parents, int children) {
		if (node == null || !containsVertex(node))
			return false;

		return inDegreeOf(node) == parents
				&& outDegreeOf(node) == children;
	}
	
	// utilities
	
	public V getRandomVertex() {
		for(V v : vertexSet())
			return v;
		
		return null;
	}
	
	public int getDistance(V v1, V v2) {
		DijkstraShortestPath<V, DefaultEdge> d = new DijkstraShortestPath<V, DefaultEdge>(
		        graph, v1, v2);
		return (int) Math.round(d.getPathLength());
	}
	
	public boolean isDirectSuccessor(V v1, V v2) {
		
		return (containsEdge(v1, v2) && inDegreeOf(v2) == 1);
	}

	// TODO make like determineEntry/ExitPoints
	
	public Set<V> determineBranches() {
		Set<V> r = new HashSet<V>();

		for (V instruction : vertexSet())
			if (outDegreeOf(instruction) > 1)
				r.add(instruction);

		return r;
	}

	public Set<V> determineJoins() {
		Set<V> r = new HashSet<V>();

		for (V instruction : vertexSet())
			if (inDegreeOf(instruction) > 1)
				r.add(instruction);

		return r;
	}

	// building up the reverse graph

	/**
	 * Returns a reverted version of this graph in a jGraph
	 * 
	 * That is a graph containing exactly the same nodes as this one but for
	 * each edge from v1 to v2 in this graph the resulting graph will contain an
	 * edge from v2 to v1 - or in other words the reverted edge
	 * 
	 * This is used to revert CFGs in order to determine control dependencies
	 * for example
	 */
	protected DefaultDirectedGraph<V,DefaultEdge> computeReverseJGraph() {
		
		DefaultDirectedGraph<V, DefaultEdge> r = new DefaultDirectedGraph<V, DefaultEdge>(
				DefaultEdge.class);
		
		for (V v : vertexSet())
			if (!r.addVertex(v))
				throw new IllegalStateException(
						"internal error while adding vertices");

		for (DefaultEdge e : edgeSet()) {
			V src = getEdgeSource(e);
			V target = getEdgeTarget(e);
			if (r.addEdge(target, src) == null)
				throw new IllegalStateException(
						"internal error while adding reverse edges");
		}
		
		return r;
	}
	
}
