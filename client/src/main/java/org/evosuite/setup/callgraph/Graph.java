package org.evosuite.setup.callgraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
/**
 * 
 * simple implementation of a graph where edges are not classes.
 * @author mattia
 *
 * @param <E>
 */
public abstract class Graph<E> {

	private final Map<E, Set<E>> edges = new HashMap<E, Set<E>>();
	private final Set<E> vertexSet = new HashSet<E>();
	
	public Map<E, Set<E>> getEdges() {
		return edges;
	}
	
	public void removeVertex(E vertex) {
		edges.remove(vertex);
		vertexSet.remove(vertex);
	}

	public boolean containsEdge(E src, E dest){
		Set<E> tempSet = edges.get(src);
		if(tempSet==null)
			return false;
		else return tempSet.contains(dest);
	}
	
	public void addEdge(E src, E dest) {
		vertexSet.add(src);
		vertexSet.add(dest);
		Set<E> srcNeighbors = this.edges.get(src);
		if (srcNeighbors == null) {
			this.edges.put(src, srcNeighbors = new LinkedHashSet<E>());
		}
		srcNeighbors.add(dest);
	}
	
	public Set<E> getVertexSet() {
		return vertexSet;
	}
	
	public boolean containsVertex(E e){
		return vertexSet.contains(e);
	}

	public Iterable<E> getNeighbors(E vertex) {
		Set<E> neighbors = this.edges.get(vertex);
		if (neighbors == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableSet(neighbors);
		}
	}

	public int getNeighborsSize(E vertex) {
		if (this.edges.get(vertex) == null)
			return 0;
		return this.edges.get(vertex).size();
	}
}
