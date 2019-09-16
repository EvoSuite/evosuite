/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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
 * @param <V> type of vertices used by the graph
 */
public abstract class Graph<V> {

	private final Map<V, Set<V>> edges = Collections.synchronizedMap(new HashMap<>());
	private final Map<V, Set<V>> reverseEdges = Collections.synchronizedMap(new HashMap<>());
	private final Set<V> vertexSet = Collections.synchronizedSet(new HashSet<>());

	public Map<V, Set<V>> getEdges() {
		return edges;
	}

	public synchronized void removeVertex(V vertex) {
		edges.remove(vertex);
		reverseEdges.remove(vertex);
		vertexSet.remove(vertex);
	}

	public synchronized boolean containsEdge(V src, V dest){
		Set<V> tempSet = edges.get(src);
		if(tempSet==null)
			return false;
		else return tempSet.contains(dest);
	}

	public synchronized void addEdge(V src, V dest) {
		vertexSet.add(src);
		vertexSet.add(dest);
		Set<V> srcNeighbors = this.edges.computeIfAbsent(src, k -> new LinkedHashSet<>());
		srcNeighbors.add(dest);

		Set<V> rsrcNeighbors = this.reverseEdges.computeIfAbsent(dest, k -> new LinkedHashSet<>());
		rsrcNeighbors.add(src);
	}

	public synchronized Set<V> getVertexSet() {
		return vertexSet;
	}

	public synchronized boolean containsVertex(V e){
		return vertexSet.contains(e);
	}

	public synchronized Set<V> getNeighbors(V vertex) {
		Set<V> neighbors = this.edges.get(vertex);
		if (neighbors == null) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableSet(neighbors);
		}
	}

	public synchronized Set<V> getReverseNeighbors(V vertex) {
		Set<V> neighbors = this.reverseEdges.get(vertex);
		if (neighbors == null) {
			return Collections.emptySet();
		} else {
			return Collections.unmodifiableSet(neighbors);
		}
	}

	public synchronized int getNeighborsSize(V vertex) {
		if (this.edges.get(vertex) == null)
			return 0;
		return this.edges.get(vertex).size();
	}

	public synchronized int getReverseNeighborsSize(V vertex) {
		if (this.reverseEdges.get(vertex) == null)
			return 0;
		return this.reverseEdges.get(vertex).size();
	}
}
