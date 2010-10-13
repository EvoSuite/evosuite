/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * EvoSuite is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with EvoSuite.  If not, see <http://www.gnu.org/licenses/>.
 */


package de.unisb.cs.st.evosuite.cfg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

public class ControlFlowGraph {

	private static Logger logger = Logger.getLogger(ControlFlowGraph.class);
	
	private DirectedGraph<CFGVertex,DefaultEdge> graph = new DirectedMultigraph<CFGVertex, DefaultEdge>(DefaultEdge.class);
	
	private int diameter = 0;

	public ControlFlowGraph(DirectedGraph<CFGVertex,DefaultEdge> cfg) {
		graph = cfg;
		setDiameter();

		// Calculate mutation distances
		logger.trace("Calculating mutation distances");
		for(CFGVertex m : cfg.vertexSet()) {
			if(m.isMutation()) {
				for(Long id : m.getMutationIds()) {
					for(CFGVertex v : cfg.vertexSet()) {
						DijkstraShortestPath<CFGVertex,DefaultEdge> d = new DijkstraShortestPath<CFGVertex,DefaultEdge>(graph, v, m);
						int distance = (int)Math.round(d.getPathLength());
						if(distance >= 0)
							v.setDistance(id, distance);
						else
							v.setDistance(id, diameter);
					}
				}
			}
		}
	}


	public int getDiameter() {
		return diameter;
	}
	
	private void setDiameter() {
		FloydWarshall<CFGVertex,DefaultEdge> f = new FloydWarshall<CFGVertex,DefaultEdge>(graph);
		diameter = (int) f.getDiameter();
	}
	
	public CFGVertex getMutation(long id) {
		for(CFGVertex v : graph.vertexSet()) {
			if(v.isMutation()) {
				if(v.hasMutation(id)) {
					return v;
				}
			}
		}
		return null;
	}
	
	public List<Long> getMutations() {
		List<Long> ids = new ArrayList<Long>();
		for(CFGVertex v : graph.vertexSet()) {
			if(v.isMutation())
				ids.addAll(v.getMutationIds());
		}
		return ids;
	}
	
	public boolean containsMutation(long id) {
		for(CFGVertex v : graph.vertexSet()) {
			if(v.isMutation() && v.hasMutation(id))
				return true;
		}
		return false;
	}
	
	private CFGVertex getBranchVertex(int number) {
		for(CFGVertex v : graph.vertexSet()) {
			if(v.isBranch() && v.getBranchId() == number) {
				return v;
			}
		}
		return null;
	}

	public CFGVertex getVertex(int id) {
		for(CFGVertex v : graph.vertexSet()) {
			if(v.id == id) {
				return v;
			}
		}
		return null;
	}

	/**
	 * Return number of control dependent branches between path and mutation
	 * @param path
	 * @param mutation
	 * @return
	 */
	public int getControlDistance(List<Integer> path, long mutation) {
		CFGVertex m = getMutation(mutation);
		if(m == null) {
			logger.warn("Could not find mutation");
			return 0;
		}
		
		int min = Integer.MAX_VALUE;
		for(Integer i : path) {
//			CFGVertex v = getBranchVertex(i);
			CFGVertex v = getVertex(i);
			if(v != null) {
				int distance = v.getDistance(mutation);
				//logger.info("Distance from "+i+": "+distance);
				if(distance < min) {
					min = distance;
				}
			} else {
				logger.warn("Could not find vertex "+i);
				for(CFGVertex vertex : graph.vertexSet()) {
					logger.warn("  -> "+vertex.toString());
				}
			}
		}
		
		return min;
	}
	
	/**
	 * Return number of control dependent branches between path and mutation
	 * @param path
	 * @param vertex
	 * @return
	 */
	public int getControlDistance(List<Integer> path, int vertex) {
		CFGVertex m = getVertex(vertex);
		if(m == null) {
			logger.warn("Vertex does not exist in graph: "+vertex);
			for(CFGVertex v : graph.vertexSet()) {
				logger.info("  Vertex id: "+v.id+", line number "+v.line_no+", branch id: "+v.getBranchId());
			}
			return diameter;
		}
		/*
		for(CFGVertex v : graph.vertexSet()) {
			logger.info("Graph vertex: "+v.id);
			for(DefaultEdge edge : graph.outgoingEdgesOf(v)) {
				logger.info("  -> "+graph.getEdgeTarget(edge).id);
			}
		}
		*/
		
		int min = Integer.MAX_VALUE;
		for(Integer i : path) {
			logger.info("Current step in path: "+i+", looking for "+vertex);
			// FIXME: Problem: i is not a bytecode id, but a branch ID (or a line id). What can we do?
			CFGVertex v = getVertex(i);
			if(v != null) {
				// FIXME: This does not actually calculate the distance!
				//int distance = v.getDistance(vertex);
				DijkstraShortestPath<CFGVertex,DefaultEdge> d = new DijkstraShortestPath<CFGVertex,DefaultEdge>(graph, v, m);
				int distance = (int)Math.round(d.getPathLength());
				logger.info("Path vertex "+i+" has distance: "+distance);
				if(distance < min && distance >= 0) {
					min = distance;
				}
			} else {
				logger.info("Could not find path vertex "+i);
			}
		}
		
		return min;
	}

	/**
	 * Return the distance to setting the last branch on the right track towards the mutation
	 * @param path
	 * @param mutation
	 * @return
	 */
	public double getBranchDistance(List<Integer> path, List<Double> distances, long mutation) {
		CFGVertex m = getMutation(mutation);
		if(m == null)
			return 0.0;
		
		int min  = Integer.MAX_VALUE;
		double dist = Double.MAX_VALUE;
		for(int i = 0; i<path.size(); i++) {
			CFGVertex v = getBranchVertex(path.get(i));
			if(v != null) {
				int distance = v.getDistance(mutation);
				if(distance < min) {
					min = distance;
					dist = distances.get(i);
				}

			}
		}
		
		return dist;
	}

	/**
	 * Return the distance to setting the last branch on the right track towards the mutation
	 * @param path
	 * @param mutation
	 * @return
	 */
	public double getBranchDistance(List<Integer> path, List<Double> distances, int branch_id) {
		CFGVertex m = getVertex(branch_id);
		if(m == null) {
			logger.info("Could not find branch node");
			return 0.0;
		}
		
		int min  = Integer.MAX_VALUE;
		double dist = Double.MAX_VALUE;
		for(int i = 0; i<path.size(); i++) {
			CFGVertex v = getVertex(path.get(i));
			if(v != null) {
				DijkstraShortestPath<CFGVertex,DefaultEdge> d = new DijkstraShortestPath<CFGVertex,DefaultEdge>(graph, v, m);
				int distance = (int)Math.round(d.getPathLength());

				if(distance < min && distance >= 0) {
					min = distance;
					dist = distances.get(i);
					logger.info("B: Path vertex "+i+" has distance: "+distance+" and branch distance "+dist);
				}
			} else {
				logger.info("Path vertex does not exist in graph");
			}
		}
		
		return dist;
	}
		
	public boolean isSuccessor(CFGVertex v1, CFGVertex v2) {
		return (graph.containsEdge(v1, v2) && graph.inDegreeOf(v2) == 1);
	}
	
	public int getDistance(CFGVertex v1, CFGVertex v2) {
		DijkstraShortestPath<CFGVertex,DefaultEdge> d = new DijkstraShortestPath<CFGVertex,DefaultEdge>(graph, v1, v2);
		return (int)Math.round(d.getPathLength());
	}
	
	public int getInitialDistance(CFGVertex v) {
		int minimum = diameter;
		
		for(CFGVertex node : graph.vertexSet()) {
			if(graph.inDegreeOf(node) == 0) {
				DijkstraShortestPath<CFGVertex,DefaultEdge> d = new DijkstraShortestPath<CFGVertex,DefaultEdge>(graph, node, v);
				int distance = (int)Math.round(d.getPathLength());
				if(distance < minimum)
					minimum = distance;
			}
		}
		return minimum;		
	}
	

	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(DefaultEdge e : graph.edgeSet()) {
			sb.append(graph.getEdgeSource(e)+" -> "+graph.getEdgeTarget(e));
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public void toDot(String filename) {

		try {
			
			FileWriter fstream = new FileWriter(filename);
	        BufferedWriter out = new BufferedWriter(fstream);
	    	if(!graph.vertexSet().isEmpty()) {
	    		//FrameVertexNameProvider nameprovider = new FrameVertexNameProvider(mn.instructions);
	    		//	DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>();
	    		//DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>(new IntegerNameProvider(), nameprovider, new IntegerEdgeNameProvider());
	    		//			DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>(new LineNumberProvider(), new LineNumberProvider(), new IntegerEdgeNameProvider());
	    		DOTExporter<CFGVertex, DefaultEdge> exporter = new DOTExporter<CFGVertex, DefaultEdge>(new IntegerNameProvider(), new StringNameProvider(), new IntegerEdgeNameProvider());
	    		exporter.export(out, graph);
	    	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
