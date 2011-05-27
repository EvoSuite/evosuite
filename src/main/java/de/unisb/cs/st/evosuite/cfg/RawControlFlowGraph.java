package de.unisb.cs.st.evosuite.cfg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.log4j.Logger;
import org.jgrapht.alg.DijkstraShortestPath;

import de.unisb.cs.st.evosuite.coverage.dataflow.DefUse;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseFactory;
import de.unisb.cs.st.evosuite.coverage.dataflow.Definition;
import de.unisb.cs.st.evosuite.coverage.dataflow.Use;


/**
 * Represents the complete CFG of a method
 * 
 * Essentially this is a graph containing all BytecodeInstrucions of a method
 * as nodes. From each such instruction there is an edge to each possible
 * instruction the control flow can reach immediately after that instruction.
 * 
 * 
 * @author Andre Mis
 */
public class RawControlFlowGraph extends
		ControlFlowGraph<BytecodeInstruction> {

	private static Logger logger = Logger.getLogger(RawControlFlowGraph.class);

	
	public RawControlFlowGraph(String className, String methodName) {
		super(className, methodName);
	}

	// inherited from ControlFlowGraph
	
	@Override
	public boolean containsInstruction(BytecodeInstruction instruction) {
		
		return containsVertex(instruction);
	}	

	@Override
	public BytecodeInstruction getInstruction(int instructionId) {
		for (BytecodeInstruction v : vertexSet()) {
			if (v.getId() == instructionId) {
				return v;
			}
		}
		return null;
	}	
	
	@Override
	public BytecodeInstruction getBranch(int branchId) {
		for (BytecodeInstruction v : vertexSet()) {
			if (v.isBranch() && v.getBranchId() == branchId) {
				return v;
			}
		}
		return null;
	}
	
	protected ControlFlowEdge addEdge(BytecodeInstruction src, BytecodeInstruction target) {
		
		ControlFlowEdge e = new ControlFlowEdge();
		if(src.isActualBranch()) {
			e.setBranchInstruction(src);
			// TODO unsafe, make better!
			e.setBranchExpressionValue(!isNonJumpEdge(src,target));
		}
		
		if(!super.addEdge(src, target, e))
			throw new IllegalStateException("internal error while adding RawCFG edge: "+e.toString());
		
		return e;
	}
	
	private boolean isNonJumpEdge(BytecodeInstruction src,
			BytecodeInstruction dst) {
		
		return Math.abs(src.getInstructionId()-dst.getInstructionId()) == 1;
	}

	// functionality used to create ActualControlFlowGraph

	public BasicBlock determineBasicBlockFor(BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");

		// TODO clean this up
		
		logger.debug("creating basic block for " + instruction.toString());

		List<BytecodeInstruction> blockNodes = new ArrayList<BytecodeInstruction>();
		blockNodes.add(instruction);

		Set<BytecodeInstruction> handled = new HashSet<BytecodeInstruction>();

		Queue<BytecodeInstruction> queue = new LinkedList<BytecodeInstruction>();
		queue.add(instruction);
		while (!queue.isEmpty()) {
			BytecodeInstruction current = queue.poll();
			handled.add(current);

			// add child to queue
			if (inDegreeOf(current) == 1)
				for (ControlFlowEdge edge : incomingEdgesOf(current)) {
					// this must be only one edge if inDegree was 1
					BytecodeInstruction parent = getEdgeSource(edge);
					if (handled.contains(parent))
						continue;
					handled.add(parent);

					if(outDegreeOf(parent)<2) {
						// insert child right before current
						// ... always thought ArrayList had insertBefore() and insertAfter() methods ... well
						blockNodes.add(blockNodes.indexOf(current), parent);
						
						queue.add(parent);
					}
				}

			// add parent to queue
			if (outDegreeOf(current) == 1)
				for (ControlFlowEdge edge : outgoingEdgesOf(current)) {
					// this must be only one edge if outDegree was 1
					BytecodeInstruction child = getEdgeTarget(edge);
					if (handled.contains(child))
						continue;
					handled.add(child);

					if(inDegreeOf(child)<2) {
						// insert parent right after current
						blockNodes.add(blockNodes.indexOf(current) + 1, child);
						
						queue.add(child);
					}
				}
		}

		BasicBlock r = new BasicBlock(className, methodName, blockNodes);

		logger.debug("created nodeBlock: "+r.toString());
		return r;
	}		
	
	// control distance functionality
	
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
			BytecodeInstruction v = getInstruction(i);
			if (v != null) {
				int distance = v.getDistance(mutation);
				//logger.info("Distance from "+i+": "+distance);
				if (distance < min) {
					min = distance;
				}
			} else {
				logger.warn("Could not find vertex " + i);
				for (BytecodeInstruction vertex : vertexSet()) {
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
		BytecodeInstruction m = getInstruction(vertex);
		if (m == null) {
			logger.warn("Vertex does not exist in graph: " + vertex);
			for (BytecodeInstruction v : graph.vertexSet()) {
				logger.info("  Vertex id: " + v.getId() + ", line number " + v.lineNumber
				        + ", branch id: " + v.getBranchId());
			}
			return getDiameter();
		}
		/*
		for(CFGVertex v : graph.vertexSet()) {
			logger.info("Graph vertex: "+v.id);
			for(ControlFlowEdge edge : graph.outgoingEdgesOf(v)) {
				logger.info("  -> "+graph.getEdgeTarget(edge).id);
			}
		}
		*/

		int min = Integer.MAX_VALUE;
		for (Integer i : path) {
			logger.info("Current step in path: " + i + ", looking for " + vertex);
			// FIXME: Problem: i is not a bytecode id, but a branch ID (or a line id). What can we do?
			BytecodeInstruction v = getInstruction(i);
			if (v != null) {
				// FIXME: This does not actually calculate the distance!
				//int distance = v.getDistance(vertex);
				DijkstraShortestPath<BytecodeInstruction, ControlFlowEdge> d = new DijkstraShortestPath<BytecodeInstruction, ControlFlowEdge>(
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
			BytecodeInstruction v = getBranch(path.get(i));
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
		BytecodeInstruction m = getInstruction(branch_id);
		if (m == null) {
			logger.info("Could not find branch node");
			return 0.0;
		}

		int min = Integer.MAX_VALUE;
		double dist = Double.MAX_VALUE;
		for (int i = 0; i < path.size(); i++) {
			BytecodeInstruction v = getInstruction(path.get(i));
			if (v != null) {
				DijkstraShortestPath<BytecodeInstruction, ControlFlowEdge> d = new DijkstraShortestPath<BytecodeInstruction, ControlFlowEdge>(
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

	public int getInitialDistance(BytecodeInstruction v) {
		int minimum = getDiameter();

		for (BytecodeInstruction node : graph.vertexSet()) {
			if (graph.inDegreeOf(node) == 0) {
				DijkstraShortestPath<BytecodeInstruction, ControlFlowEdge> d = new DijkstraShortestPath<BytecodeInstruction, ControlFlowEdge>(
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
			Set<ControlFlowEdge> incomingEdges = graph.incomingEdgesOf(current);
			for(ControlFlowEdge incomingEdge : incomingEdges) {
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
			Set<ControlFlowEdge> outgoingEdges = graph.outgoingEdgesOf(current);
			for(ControlFlowEdge outgoingEdge : outgoingEdges) {
				BytecodeInstruction target = graph.getEdgeTarget(outgoingEdge);
				if(target.getId() < current.getId())
					continue;
				queue.add(target);
			}
			visited.add(current);
		}
		return visited;
	}	

	// functionality for defUse coverage
	
	public Set<Use> getUsesForDef(Definition def) {
		if (!graph.containsVertex(def))
			throw new IllegalArgumentException("unknown Definition");

		return getUsesForDef(def, def);
	}

	private Set<Use> getUsesForDef(Definition targetDef, BytecodeInstruction entry) {
		if (!graph.containsVertex(entry))
			throw new IllegalArgumentException("vertex not in graph");
		
		Set<Use> r = new HashSet<Use>();
		
		Set<ControlFlowEdge> outgoingEdges = graph.outgoingEdgesOf(entry);
		for (ControlFlowEdge e : outgoingEdges) {
			BytecodeInstruction edgeTarget = graph.getEdgeTarget(e);
			
			if(edgeTarget.isDefUse()) {
				if (targetDef.canBeActiveFor(edgeTarget))
					r.add(DefUseFactory.makeUse(edgeTarget));
				if (targetDef.canBecomeActiveDefinition(edgeTarget))
					continue;
			}
			if (edgeTarget.getInstructionId() > entry.getInstructionId()) // dont follow backedges (loops)
				r.addAll(getUsesForDef(targetDef, edgeTarget));			
		}
		return r;
	}
	
	public boolean hasDefClearPathToMethodExit(DefUse duVertex) {
		if (!graph.containsVertex(duVertex))
			throw new IllegalArgumentException("vertex not in graph");
		if (duVertex.isLocalDU())
			return false;

		return hasDefClearPathToMethodExit(duVertex, duVertex);
	}

	public boolean hasDefClearPathFromMethodEntry(DefUse duVertex) {
		if (!graph.containsVertex(duVertex))
			throw new IllegalArgumentException("vertex not in graph");
		if (duVertex.isLocalDU())
			return false;

		return hasDefClearPathFromMethodEntry(duVertex, duVertex);
	}

	private boolean hasDefClearPathToMethodExit(DefUse targetDefUse, BytecodeInstruction currentVertex) {
		if (!graph.containsVertex(currentVertex))
			throw new IllegalArgumentException("vertex not in graph");
		
		// TODO corner case when this method is initially called with a definition? 
		// .. which should never happen cause this method is meant to be called for uses ... 
		// TODO make this explicit
		
		Set<ControlFlowEdge> outgoingEdges = graph.outgoingEdgesOf(currentVertex);
		if (outgoingEdges.size() == 0)
			return true;

		for (ControlFlowEdge e : outgoingEdges) {
			BytecodeInstruction edgeTarget = graph.getEdgeTarget(e);

			// skip edges going into another def for the same field
			if (targetDefUse.canBecomeActiveDefinition(edgeTarget))
					continue;
			
			if (edgeTarget.getId() > currentVertex.getId() // dont follow backedges (loops)
			        && hasDefClearPathToMethodExit(targetDefUse, edgeTarget))
				return true;
		}
		return false;
	}

	private boolean hasDefClearPathFromMethodEntry(DefUse targetDefUse, BytecodeInstruction currentVertex) {
		if (!graph.containsVertex(currentVertex))
			throw new IllegalArgumentException("vertex not in graph");
		
		Set<ControlFlowEdge> incomingEdges = graph.incomingEdgesOf(currentVertex);
		if (incomingEdges.size() == 0)
			return true;

		for (ControlFlowEdge e : incomingEdges) {
			BytecodeInstruction edgeStart = graph.getEdgeSource(e);

			// skip edges coming from a def for the same field
			if (targetDefUse.canBecomeActiveDefinition(edgeStart))
					continue;

			if (edgeStart.getId() < currentVertex.getId() // dont follow backedges (loops) 
			        && hasDefClearPathFromMethodEntry(targetDefUse, edgeStart))
				return true;
		}
		return false;
	}

	// former broken CDG functionality
	
	/**
	 * WARNING currently this method is heavily flawed! Only works on very
	 * simple (generic) CFGs
	 * 
	 */
	public void markBranchIds(BytecodeInstruction branch) {
		// TODO clean this mess up!
		
		if(!graph.containsVertex(branch))
			throw new IllegalArgumentException("unknown branch");
		if (branch.getBranchId() == -1)
			throw new IllegalArgumentException("expect branch to have branchID set");
		
		Set<ControlFlowEdge> out = graph.outgoingEdgesOf(branch);

		// TODO: this is not correct. FIX THIS! 
		//if (out.size() < 2)
		//	throw new IllegalStateException(
		//	        "expect branchVertices to have exactly two outgoing edges");

		int minID = Integer.MAX_VALUE;
		int maxID = Integer.MIN_VALUE;
		for (ControlFlowEdge e : out) {
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
			BytecodeInstruction prevVertex = getInstruction(maxID - 1);
			if (prevVertex.isGoto()) {
				//				logger.error("WITH ELSE PART");
				Set<ControlFlowEdge> prevOut = graph.outgoingEdgesOf(prevVertex);
				if (prevOut.size() != 1)
					throw new IllegalStateException(
					        "expect gotos to only have 1 outgoing edge");
				ControlFlowEdge elseEnd = null;
				for (ControlFlowEdge e : prevOut)
					elseEnd = e;
				markNodes(maxID + 1, graph.getEdgeTarget(elseEnd).getId(),
				          branch, false);
			}
		}
	}

	private void markNodes(int start, int end, BytecodeInstruction branch, boolean branchExpressionValue) {
		for (int i = start; i <= end; i++) {
			BytecodeInstruction v = getInstruction(i);
			if (v != null) {
				v.branchId = branch.getBranchId();
				v.branchExpressionValue = branchExpressionValue;
//				v.addControlDependentBranch(branch);
			}
		}
	}

	private boolean isIfBranch(int maxID) {
		BytecodeInstruction prevVertex = getInstruction(maxID - 1);
		if (!graph.containsVertex(prevVertex))
			return false;
		Set<ControlFlowEdge> prevOut = graph.outgoingEdgesOf(prevVertex);
		if (prevOut.size() != 1) {
			return false;
		}
		ControlFlowEdge backEdge = null;
		for (ControlFlowEdge e : prevOut)
			backEdge = e;
		// only if-branches have this structure
		return !(graph.getEdgeTarget(backEdge).getId() < maxID);
	}
	
	/*
	private boolean isWhileBranch(int maxID) {
		BytecodeInstruction prevVertex = getInstruction(maxID - 1);
		Set<ControlFlowEdge> prevOut = graph.outgoingEdgesOf(prevVertex);
		if (prevOut.size() != 1) {
			return false;
		}
		ControlFlowEdge backEdge = null;
		for (ControlFlowEdge e : prevOut)
			backEdge = e;
		// only while-branches go back up
		return graph.getEdgeTarget(backEdge).getId() < maxID;
	}
	*/
	
	// miscellaneous

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (ControlFlowEdge e : graph.edgeSet()) {
			sb.append(graph.getEdgeSource(e) + " -> " + graph.getEdgeTarget(e));
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public String getName() {
		return "RawCFG"+graphId; // TODO make nice
	}
}
