package org.evosuite.setup.callgraph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
/**
 * 
 * @author mattia
 *
 * @param <E>
 */
public abstract class Graph<E> {

	private final Map<E, Set<E>> edges = new HashMap<E, Set<E>>();
	private final Set<E> edgesSet = new HashSet<E>();
	
	public Map<E, Set<E>> getEdges() {
		return edges;
	}
	
	public void addEdge(E src, E dest) {
		edgesSet.add(src);
		edgesSet.add(dest);
		Set<E> srcNeighbors = this.edges.get(src);
		if (srcNeighbors == null) {
			this.edges.put(src, srcNeighbors = new LinkedHashSet<E>());
		}
		srcNeighbors.add(dest);
	}
	
	public boolean containsVertex(E e){
		return edgesSet.contains(e);
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
