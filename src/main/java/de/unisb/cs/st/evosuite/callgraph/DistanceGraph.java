/*
* Copyright (C) 2011 Saarland University
* 
* This file is part of Javalanche.
* 
* Javalanche is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Javalanche is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser Public License for more details.
* 
* You should have received a copy of the GNU Lesser Public License
* along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
*/
package de.unisb.cs.st.evosuite.callgraph;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class DistanceGraph {

	private static Logger logger = LoggerFactory.getLogger(DistanceGraph.class);
	// private ConnectionData data;
	private final Hierarchy hierarchy;
	private UndirectedGraph<MethodDescription, DefaultEdge> g;
	private Set<MethodDescription> allMethods;

	public DistanceGraph(ConnectionData data, Hierarchy hierarchy) {
		// this.data = data;
		this.hierarchy = hierarchy;
		allMethods = data.getAllMethods();
		g = new SimpleGraph<MethodDescription, DefaultEdge>(DefaultEdge.class);
		Set<Tuple> connections = data.getConnections();
		for (Tuple tuple : connections) {
			MethodDescription start = tuple.getStart();
			MethodDescription end = tuple.getEnd();
			if (!start.equals(end)) {
				if (!g.containsVertex(start)) {
					g.addVertex(start);
				}
				if (!g.containsVertex(end)) {
					g.addVertex(end);
				}
				g.addEdge(start, end);
			}
		}
	}

	public int getDistance(MethodDescription start, MethodDescription end) {
		if (!(g.containsVertex(start) && g.containsVertex(end))) {
			return -1;
		}
		DijkstraShortestPath<MethodDescription, DefaultEdge> sp = new DijkstraShortestPath<MethodDescription, DefaultEdge>(
				g, start, end);
		double pathLength = sp.getPathLength();
		System.out.println("DistanceGraph.getDistance() "
				+ sp.getPathEdgeList());
		if (pathLength == Double.POSITIVE_INFINITY) {
			pathLength = 0;
			Set<String> allSupers = hierarchy.getAllSupers(end.getClassName());
			for (String sup : allSupers) {
				MethodDescription super1 = end.getSuper(sup);
				int distance = getDistance(start, super1);
				if (distance > 0) {
					return distance;
				}
			}
		}
		if (pathLength < 0.) {
			pathLength = 0;
		}
		return (int) pathLength;
	}

	public static DistanceGraph getDefault() {
		ConnectionData data = ConnectionData.read();
		Hierarchy hierarchy = Hierarchy.readFromDefaultLocation();
		return new DistanceGraph(data, hierarchy);
	}

	public MethodDescription getMetodDesc(String fullMethodName) {
		String className = fullMethodName.substring(0, fullMethodName
				.indexOf('@'));
		String methodName = fullMethodName.substring(fullMethodName
				.indexOf('@') + 1);

		for (MethodDescription md : allMethods) {
			if (className.equals(md.getClassName())) {
				if (methodName.equals(md.getMethodName())) {
					return md;
				}
			}

		}
		logger.warn("Not Found " + className + "   " + methodName + " "
				+ fullMethodName);
		return null;
	}

}
