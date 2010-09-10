package de.unisb.cs.st.evosuite.testcase;

import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import de.unisb.cs.st.javalanche.coverage.distance.ConnectionData;
import de.unisb.cs.st.javalanche.coverage.distance.Hierarchy;
import de.unisb.cs.st.javalanche.coverage.distance.MethodDescription;
import de.unisb.cs.st.javalanche.coverage.distance.Tuple;

public class MethodDistanceGraph {

	private static Logger logger = Logger.getLogger(MethodDistanceGraph.class);

	private static MethodDistanceGraph instance = null;
	
	private DirectedGraph<MethodDescription, DefaultEdge> graph;
	
	private Set<MethodDescription> allMethods;
	
	private final Hierarchy hierarchy;
	
	private MethodDistanceGraph() {
		hierarchy = Hierarchy.readFromDefaultLocation();
		createGraph();
	}
	
	private void createGraph() {
		ConnectionData data = ConnectionData.read();
		allMethods = data.getAllMethods();
		graph = new DefaultDirectedGraph<MethodDescription, DefaultEdge>(DefaultEdge.class);
		Set<Tuple> connections = data.getConnections();
		for (Tuple tuple : connections) {
			MethodDescription start = tuple.getStart();
			MethodDescription end = tuple.getEnd();
			if (!start.equals(end)) {
				if (!graph.containsVertex(start)) {
					graph.addVertex(start);
				}
				if (!graph.containsVertex(end)) {
					graph.addVertex(end);
				}
				//graph.addEdge(start, end);
	//			System.out.println(end+" calls "+start);
//				graph.addEdge(end, start);
				graph.addEdge(start, end); // TODO: Which way round?
			}
		}
	//	addInheritanceEdges();
		for(MethodDescription m : allMethods) {
			if(!graph.containsVertex(m))
				graph.addVertex(m);
//				logger.warn("Graph does not contain node for "+m);
		}
	}

	private void addInheritanceEdges() {
		for(MethodDescription m : graph.vertexSet()) {
			String classname = m.getClassName();
			Set<String> supers = hierarchy.getAllSupers(classname);
			for(String superclass : supers) {
				MethodDescription super_m = m.getSuper(superclass);
				if(graph.containsVertex(super_m)) {
					logger.info("Added inheritance edge from "+m+" to "+super_m);
					graph.addEdge(m, super_m);
				}
			}
		}
	}

	public int getDistance(MethodDescription start, MethodDescription end) {
		if (!(graph.containsVertex(start) && graph.containsVertex(end))) {
			return Integer.MAX_VALUE;
		}
		DijkstraShortestPath<MethodDescription, DefaultEdge> sp = new DijkstraShortestPath<MethodDescription, DefaultEdge>(
				graph, start, end);
		double pathLength = sp.getPathLength();
		//logger.info("Got length: "+pathLength);
		
		if (pathLength == Double.POSITIVE_INFINITY) {
			//pathLength = 0;
			Set<String> allSupers = hierarchy.getAllSupers(end.getClassName());
			for (String sup : allSupers) {
				MethodDescription super1 = end.getSuper(sup);
				int distance = getDistance(start, super1);
				if (distance < pathLength) {
					//logger.info("Got length for super " + sup + ": "+distance);
					return distance;
				}
			}
		}
		//pathLength = Double.POSITIVE_INFINITY;
		/*
		if (pathLength < 0.) {
			pathLength = 0;
		}*/
		return (int) pathLength;
	}

	
	public static MethodDistanceGraph getMethodDistanceGraph() {
		if(instance == null) {
			instance = new MethodDistanceGraph();
		}
		return instance;
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

	public MethodDescription getMethodDesc(String className, String methodName) {
		className = className.replace('/', '.');
		for (MethodDescription md : allMethods) {
			if (className.equals(md.getClassName())) {
				String fullname = md.getMethodName() + md.getDesc();
				if (methodName.equals(fullname)) {
					return md;
				}
			}

		}
		logger.warn("Not Found " + className + "   " + methodName);
		return null;
	}
}
