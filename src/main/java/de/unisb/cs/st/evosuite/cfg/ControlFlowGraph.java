/*
 * Copyright (C) 2010 Saarland University
 * 
 * This file is part of EvoSuite.
 * 
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */

package de.unisb.cs.st.evosuite.cfg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.ext.DOTExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.DefaultEdge;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUse;
import de.unisb.cs.st.evosuite.coverage.dataflow.Definition;
import de.unisb.cs.st.evosuite.coverage.dataflow.Use;

public class ControlFlowGraph {

	private static Logger logger = Logger.getLogger(ControlFlowGraph.class);

	private final DirectedGraph<BytecodeInstruction, DefaultEdge> graph;

	private int diameter = 0;

	public ControlFlowGraph(DirectedGraph<BytecodeInstruction, DefaultEdge> cfg,
	        boolean calculateDiameter) {
		this.graph = cfg;

		if (calculateDiameter) {
			setDiameter();

			// Calculate mutation distances
			logger.trace("Calculating mutation distances");
			for (BytecodeInstruction m : cfg.vertexSet()) {
				if (m.isMutation()) {
					for (Long id : m.getMutationIds()) {
						for (BytecodeInstruction v : cfg.vertexSet()) {
							DijkstraShortestPath<BytecodeInstruction, DefaultEdge> d = new DijkstraShortestPath<BytecodeInstruction, DefaultEdge>(
							        graph, v, m);
							int distance = (int) Math.round(d.getPathLength());
							if (distance >= 0)
								v.setDistance(id, distance);
							else
								v.setDistance(id, diameter);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the underlying graph. 
	 * Note that this is not a clone but the real graph, it SHOULD NOT be modified.
	 * @return
	 */
	public DirectedGraph<BytecodeInstruction, DefaultEdge> getGraph(){
		return graph;
	}
	
	public int getDiameter() {
		return diameter;
	}

	private void setDiameter() {
		FloydWarshall<BytecodeInstruction, DefaultEdge> f = new FloydWarshall<BytecodeInstruction, DefaultEdge>(
		        graph);
		diameter = (int) f.getDiameter();
	}

	public BytecodeInstruction getMutation(long id) {
		for (BytecodeInstruction v : graph.vertexSet()) {
			if (v.isMutation()) {
				if (v.hasMutation(id)) {
					return v;
				}
			}
		}
		return null;
	}

	public List<Long> getMutations() {
		List<Long> ids = new ArrayList<Long>();
		for (BytecodeInstruction v : graph.vertexSet()) {
			if (v.isMutation())
				ids.addAll(v.getMutationIds());
		}
		return ids;
	}

	public boolean containsMutation(long id) {
		for (BytecodeInstruction v : graph.vertexSet()) {
			if (v.isMutation() && v.hasMutation(id))
				return true;
		}
		return false;
	}

	private BytecodeInstruction getBranchVertex(int number) {
		for (BytecodeInstruction v : graph.vertexSet()) {
			if (v.isBranch() && v.getBranchId() == number) {
				return v;
			}
		}
		return null;
	}

	public BytecodeInstruction getVertex(int id) {
		for (BytecodeInstruction v : graph.vertexSet()) {
			if (v.getId() == id) {
				return v;
			}
		}
		return null;
	}

	/**
	 * Return number of control dependent branches between path and mutation
	 * 
	 * @param path
	 * @param mutation
	 * @return
	 */
	public int getControlDistance(List<Integer> path, long mutation) {
		BytecodeInstruction m = getMutation(mutation);
		if (m == null) {
			logger.warn("Could not find mutation");
			return 0;
		}

		int min = Integer.MAX_VALUE;
		for (Integer i : path) {
			//			CFGVertex v = getBranchVertex(i);
			BytecodeInstruction v = getVertex(i);
			if (v != null) {
				int distance = v.getDistance(mutation);
				//logger.info("Distance from "+i+": "+distance);
				if (distance < min) {
					min = distance;
				}
			} else {
				logger.warn("Could not find vertex " + i);
				for (BytecodeInstruction vertex : graph.vertexSet()) {
					logger.warn("  -> " + vertex.toString());
				}
			}
		}

		return min;
	}

	/**
	 * Return number of control dependent branches between path and mutation
	 * 
	 * @param path
	 * @param vertex
	 * @return
	 */
	public int getControlDistance(List<Integer> path, int vertex) {
		BytecodeInstruction m = getVertex(vertex);
		if (m == null) {
			logger.warn("Vertex does not exist in graph: " + vertex);
			for (BytecodeInstruction v : graph.vertexSet()) {
				logger.info("  Vertex id: " + v.getId() + ", line number " + v.lineNumber
				        + ", branch id: " + v.getBranchId());
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
		for (Integer i : path) {
			logger.info("Current step in path: " + i + ", looking for " + vertex);
			// FIXME: Problem: i is not a bytecode id, but a branch ID (or a line id). What can we do?
			BytecodeInstruction v = getVertex(i);
			if (v != null) {
				// FIXME: This does not actually calculate the distance!
				//int distance = v.getDistance(vertex);
				DijkstraShortestPath<BytecodeInstruction, DefaultEdge> d = new DijkstraShortestPath<BytecodeInstruction, DefaultEdge>(
				        graph, v, m);
				int distance = (int) Math.round(d.getPathLength());
				logger.info("Path vertex " + i + " has distance: " + distance);
				if (distance < min && distance >= 0) {
					min = distance;
				}
			} else {
				logger.info("Could not find path vertex " + i);
			}
		}

		return min;
	}

	/**
	 * Return the distance to setting the last branch on the right track towards
	 * the mutation
	 * 
	 * @param path
	 * @param mutation
	 * @return
	 */
	public double getBranchDistance(List<Integer> path, List<Double> distances,
	        long mutation) {
		BytecodeInstruction m = getMutation(mutation);
		if (m == null)
			return 0.0;

		int min = Integer.MAX_VALUE;
		double dist = Double.MAX_VALUE;
		for (int i = 0; i < path.size(); i++) {
			BytecodeInstruction v = getBranchVertex(path.get(i));
			if (v != null) {
				int distance = v.getDistance(mutation);
				if (distance < min) {
					min = distance;
					dist = distances.get(i);
				}

			}
		}

		return dist;
	}

	/**
	 * Return the distance to setting the last branch on the right track towards
	 * the mutation
	 * 
	 * @param path
	 * @param mutation
	 * @return
	 */
	public double getBranchDistance(List<Integer> path, List<Double> distances,
	        int branch_id) {
		BytecodeInstruction m = getVertex(branch_id);
		if (m == null) {
			logger.info("Could not find branch node");
			return 0.0;
		}

		int min = Integer.MAX_VALUE;
		double dist = Double.MAX_VALUE;
		for (int i = 0; i < path.size(); i++) {
			BytecodeInstruction v = getVertex(path.get(i));
			if (v != null) {
				DijkstraShortestPath<BytecodeInstruction, DefaultEdge> d = new DijkstraShortestPath<BytecodeInstruction, DefaultEdge>(
				        graph, v, m);
				int distance = (int) Math.round(d.getPathLength());

				if (distance < min && distance >= 0) {
					min = distance;
					dist = distances.get(i);
					logger.info("B: Path vertex " + i + " has distance: " + distance
					        + " and branch distance " + dist);
				}
			} else {
				logger.info("Path vertex does not exist in graph");
			}
		}

		return dist;
	}

	public boolean isDirectSuccessor(BytecodeInstruction v1, BytecodeInstruction v2) {
		return (graph.containsEdge(v1, v2) && graph.inDegreeOf(v2) == 1);
	}

	public int getDistance(BytecodeInstruction v1, BytecodeInstruction v2) {
		DijkstraShortestPath<BytecodeInstruction, DefaultEdge> d = new DijkstraShortestPath<BytecodeInstruction, DefaultEdge>(
		        graph, v1, v2);
		return (int) Math.round(d.getPathLength());
	}

	public int getInitialDistance(BytecodeInstruction v) {
		int minimum = diameter;

		for (BytecodeInstruction node : graph.vertexSet()) {
			if (graph.inDegreeOf(node) == 0) {
				DijkstraShortestPath<BytecodeInstruction, DefaultEdge> d = new DijkstraShortestPath<BytecodeInstruction, DefaultEdge>(
				        graph, node, v);
				int distance = (int) Math.round(d.getPathLength());
				if (distance < minimum)
					minimum = distance;
			}
		}
		return minimum;
	}
	
	/**
	 * Returns the number of byteCode instructions that can potentially be
	 * executed from entering the method of this CFG until
	 * the given CFGVertex is reached.
	 */
	public Set<BytecodeInstruction> getPreviousInstructionsInMethod(BytecodeInstruction v) {
		Set<BytecodeInstruction> visited = new HashSet<BytecodeInstruction>();
		PriorityQueue<BytecodeInstruction> queue = new PriorityQueue<BytecodeInstruction>(graph.vertexSet().size(),new CFGVertexIdComparator());
		queue.add(v);
		while(queue.peek()!=null) {
			BytecodeInstruction current = queue.poll();
			if(visited.contains(current))
				continue;
			Set<DefaultEdge> incomingEdges = graph.incomingEdgesOf(current);
			for(DefaultEdge incomingEdge : incomingEdges) {
				BytecodeInstruction source = graph.getEdgeSource(incomingEdge);
				if(source.getId() >= current.getId())
					continue;
				queue.add(source);
			}
			visited.add(current);
		}
		return visited;
	}
	
	/**
	 * Returns the number of byteCode instructions that can potentially be
	 * executed from passing the given CFVertex until the end of
	 * the method of this CFG is reached.
	 */
	@SuppressWarnings("unchecked")
	public Set<BytecodeInstruction> getLaterInstructionsInMethod(BytecodeInstruction v) {
		Set<BytecodeInstruction> visited = new HashSet<BytecodeInstruction>();
		Comparator<BytecodeInstruction> reverseComp = new ReverseComparator(new CFGVertexIdComparator());
		PriorityQueue<BytecodeInstruction> queue = new PriorityQueue<BytecodeInstruction>(graph.vertexSet().size(),
				reverseComp);
		queue.add(v);
		while(queue.peek()!=null) {
			BytecodeInstruction current = queue.poll();
			if(visited.contains(current))
				continue;
			Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(current);
			for(DefaultEdge outgoingEdge : outgoingEdges) {
				BytecodeInstruction target = graph.getEdgeTarget(outgoingEdge);
				if(target.getId() < current.getId())
					continue;
				queue.add(target);
			}
			visited.add(current);
		}
		return visited;
	}	

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (DefaultEdge e : graph.edgeSet()) {
			sb.append(graph.getEdgeSource(e) + " -> " + graph.getEdgeTarget(e));
			sb.append("\n");
		}
		return sb.toString();
	}

	public void toDot(String filename) {

		try {

			FileWriter fstream = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(fstream);
			if (!graph.vertexSet().isEmpty()) {
				//FrameVertexNameProvider nameprovider = new FrameVertexNameProvider(mn.instructions);
				//	DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>();
				//DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>(new IntegerNameProvider(), nameprovider, new IntegerEdgeNameProvider());
				//			DOTExporter<Integer,DefaultEdge> exporter = new DOTExporter<Integer,DefaultEdge>(new LineNumberProvider(), new LineNumberProvider(), new IntegerEdgeNameProvider());
				DOTExporter<BytecodeInstruction, DefaultEdge> exporter = new DOTExporter<BytecodeInstruction, DefaultEdge>(
				        new IntegerNameProvider<BytecodeInstruction>(),
				        new StringNameProvider<BytecodeInstruction>(),
				        new IntegerEdgeNameProvider<DefaultEdge>());
				exporter.export(out, graph);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Set<Use> getUsesForDef(Definition def) {
		if (!graph.containsVertex(def))
			throw new IllegalArgumentException("unknown Definition");

		return getUsesForDef(def, def);
	}

	public boolean hasDefClearPathToMethodEnd(DefUse duVertex) {
		if (!graph.containsVertex(duVertex))
			throw new IllegalArgumentException("vertex not in graph");
		if(!duVertex.isUse())
			logger.warn("method designed for Uses, not Definitions");
		if (duVertex.isLocalDU())
			return false;

		return hasDefClearPathToMethodEnd(duVertex, duVertex);
	}

	public boolean hasDefClearPathFromMethodStart(DefUse duVertex) {
		if (!graph.containsVertex(duVertex))
			throw new IllegalArgumentException("vertex not in graph");
		if (duVertex.isLocalDU())
			return false;

		return hasDefClearPathFromMethodStart(duVertex, duVertex);
	}

	private Set<Use> getUsesForDef(Definition targetDefinition, BytecodeInstruction currentVertex) {
		if (!graph.containsVertex(currentVertex))
			throw new IllegalArgumentException("vertex not in graph");

		String varName = targetDefinition.getDUVariableName();
		
		Set<Use> r = new HashSet<Use>();
		Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(currentVertex);
		if (outgoingEdges.size() == 0)
			return r;
		for (DefaultEdge e : outgoingEdges) {
			BytecodeInstruction edgeTarget = graph.getEdgeTarget(e);
			try {
				DefUse du = (DefUse)edgeTarget;
				if (edgeTarget.isUse() && du.getDUVariableName().equals(varName))
					r.add((Use)du);
				if (edgeTarget.isDefinition()
				        && du.getDUVariableName().equals(varName))
					continue;
			} catch(ClassCastException ex) {}
			
			if (edgeTarget.getInstructionId() > currentVertex.getInstructionId()) // dont follow backedges (loops)
				r.addAll(getUsesForDef(targetDefinition, edgeTarget));			
		}
		return r;
	}

	private boolean hasDefClearPathToMethodEnd(DefUse targetDefUse, BytecodeInstruction currentVertex) {
		if (!graph.containsVertex(currentVertex))
			throw new IllegalArgumentException("vertex not in graph");
		
		String varName = targetDefUse.getDUVariableName();
		// TODO corner case when this method is initially called with a definition? 
		// .. which should never happen cause this method is meant to be called for uses ... 
		// TODO make this explicit
		
		Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(currentVertex);
		if (outgoingEdges.size() == 0)
			return true;

		for (DefaultEdge e : outgoingEdges) {
			BytecodeInstruction edgeTarget = graph.getEdgeTarget(e);
			try {
				// skip edges going into another def for the same field
				Definition def = new Definition(edgeTarget);
				if (def.getDUVariableName().equals(varName))
					continue;
				
			} catch(IllegalArgumentException ex) {}
			
			if (edgeTarget.getId() > currentVertex.getId() // dont follow backedges (loops)
			        && hasDefClearPathToMethodEnd(targetDefUse, edgeTarget))
				return true;
		}
		return false;
	}

	private boolean hasDefClearPathFromMethodStart(DefUse targetDefUse, BytecodeInstruction currentVertex) {
		if (!graph.containsVertex(currentVertex))
			throw new IllegalArgumentException("vertex not in graph");
		
		String varName = targetDefUse.getDUVariableName();

		Set<DefaultEdge> incomingEdges = graph.incomingEdgesOf(currentVertex);
		if (incomingEdges.size() == 0)
			return true;

		for (DefaultEdge e : incomingEdges) {
			BytecodeInstruction edgeStart = graph.getEdgeSource(e);
			try {
				// skip edges coming from a def for the same field
				Definition def = new Definition(edgeStart);
				if (def.getDUVariableName().equals(varName))
					continue;
			} catch(IllegalArgumentException ex) { 
				// expected
			}

			if (edgeStart.getId() < currentVertex.getId() // dont follow backedges (loops) 
			        && hasDefClearPathFromMethodStart(targetDefUse, edgeStart))
				return true;
		}
		return false;
	}

	/**
	 * WARNING currently this method is heavily flawed! Only works on very
	 * simple (generic) CFGs
	 * 
	 */
	public void markBranchIds(Branch branch) {
		// TODO clean this mess up!
		
		if(!graph.containsVertex(branch))
			throw new IllegalArgumentException("unknown branch");
		if (branch.getBranchId() == -1)
			throw new IllegalArgumentException("expect branch to have branchID set");
		
		Set<DefaultEdge> out = graph.outgoingEdgesOf(branch);

		// TODO: this is not correct. FIX THIS! 
		//if (out.size() < 2)
		//	throw new IllegalStateException(
		//	        "expect branchVertices to have exactly two outgoing edges");

		int minID = Integer.MAX_VALUE;
		int maxID = Integer.MIN_VALUE;
		for (DefaultEdge e : out) {
			BytecodeInstruction target = graph.getEdgeTarget(e);
			if (minID > target.getId())
				minID = target.getId();
			if (maxID < target.getId())
				maxID = target.getId();
		}
//		if (minID < branchVertex.id) {
//			logger.error("DO-WHILE BRANCH"+branchVertex.branchID);
//			return;
//		}
		markNodes(minID, maxID, branch, true);
//		if(isWhileBranch(maxID)) // accepts for-loops when they dont have a return
//			logger.error("WHILE BRANCH");
//		logger.error("marking branch ids");
		if (isIfBranch(maxID)) {
			//			logger.error("IF BRANCH: "+branchVertex.branchID+" bytecode "+branchVertex.id);
			BytecodeInstruction prevVertex = getVertex(maxID - 1);
			if (prevVertex.isGoto()) {
				//				logger.error("WITH ELSE PART");
				Set<DefaultEdge> prevOut = graph.outgoingEdgesOf(prevVertex);
				if (prevOut.size() != 1)
					throw new IllegalStateException(
					        "expect gotos to only have 1 outgoing edge");
				DefaultEdge elseEnd = null;
				for (DefaultEdge e : prevOut)
					elseEnd = e;
				markNodes(maxID + 1, graph.getEdgeTarget(elseEnd).getId(),
				          branch, false);
			}
		}
	}

	private void markNodes(int start, int end, Branch branch, boolean branchExpressionValue) {
		for (int i = start; i <= end; i++) {
			BytecodeInstruction v = getVertex(i);
			if (v != null) {
				v.branchId = branch.getBranchId();
				v.branchExpressionValue = branchExpressionValue;
//				v.addControlDependentBranch(branch);
			}
		}
	}

	private boolean isIfBranch(int maxID) {
		BytecodeInstruction prevVertex = getVertex(maxID - 1);
		if (!graph.containsVertex(prevVertex))
			return false;
		Set<DefaultEdge> prevOut = graph.outgoingEdgesOf(prevVertex);
		if (prevOut.size() != 1) {
			return false;
		}
		DefaultEdge backEdge = null;
		for (DefaultEdge e : prevOut)
			backEdge = e;
		// only if-branches have this structure
		return !(graph.getEdgeTarget(backEdge).getId() < maxID);
	}

	private boolean isWhileBranch(int maxID) {
		BytecodeInstruction prevVertex = getVertex(maxID - 1);
		Set<DefaultEdge> prevOut = graph.outgoingEdgesOf(prevVertex);
		if (prevOut.size() != 1) {
			return false;
		}
		DefaultEdge backEdge = null;
		for (DefaultEdge e : prevOut)
			backEdge = e;
		// only while-branches go back up
		return graph.getEdgeTarget(backEdge).getId() < maxID;
	}

}
