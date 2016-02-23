/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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

	private final Map<E, Set<E>> edges = Collections.synchronizedMap(new HashMap<E, Set<E>>());
	private final Map<E, Set<E>> reverseEdges = Collections.synchronizedMap(new HashMap<E, Set<E>>());
	private final Set<E> vertexSet = Collections.synchronizedSet(new HashSet<E>());
	
	public Map<E, Set<E>> getEdges() {
		return edges;
	}
	
	public synchronized void removeVertex(E vertex) {
		edges.remove(vertex);
		reverseEdges.remove(vertex);
		vertexSet.remove(vertex);
	}

	public synchronized boolean containsEdge(E src, E dest){
		Set<E> tempSet = edges.get(src);
		if(tempSet==null)
			return false;
		else return tempSet.contains(dest);
	}
	
	public synchronized void addEdge(E src, E dest) {
		vertexSet.add(src);
		vertexSet.add(dest);
		Set<E> srcNeighbors = this.edges.get(src);
		if (srcNeighbors == null) {
			this.edges.put(src, srcNeighbors = new LinkedHashSet<E>());
		}
		srcNeighbors.add(dest);
		
		Set<E> rsrcNeighbors = this.reverseEdges.get(dest);
		if (rsrcNeighbors == null) {
			this.reverseEdges.put(dest, rsrcNeighbors = new LinkedHashSet<E>());
		}
		rsrcNeighbors.add(src);
	}
	
	public synchronized Set<E> getVertexSet() {
		return vertexSet;
	}
	
	public synchronized boolean containsVertex(E e){
		return vertexSet.contains(e);
	}

	public synchronized Iterable<E> getNeighbors(E vertex) {
		Set<E> neighbors = this.edges.get(vertex);
		if (neighbors == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableSet(neighbors);
		}
	}
	
	public synchronized Iterable<E> getReverseNeighbors(E vertex) {
		Set<E> neighbors = this.reverseEdges.get(vertex);
		if (neighbors == null) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableSet(neighbors);
		}
	}

	public synchronized int getNeighborsSize(E vertex) {
		if (this.edges.get(vertex) == null)
			return 0;
		return this.edges.get(vertex).size();
	}
}
