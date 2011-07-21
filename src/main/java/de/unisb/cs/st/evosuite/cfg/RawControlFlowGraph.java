package de.unisb.cs.st.evosuite.cfg;

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
import org.objectweb.asm.tree.LabelNode;

import de.unisb.cs.st.evosuite.coverage.branch.Branch;
import de.unisb.cs.st.evosuite.coverage.branch.BranchPool;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUse;
import de.unisb.cs.st.evosuite.coverage.dataflow.DefUseFactory;
import de.unisb.cs.st.evosuite.coverage.dataflow.Definition;
import de.unisb.cs.st.evosuite.coverage.dataflow.Use;

/**
 * Represents the complete CFG of a method
 * 
 * Essentially this is a graph containing all BytecodeInstrucions of a method as
 * nodes. From each such instruction there is an edge to each possible
 * instruction the control flow can reach immediately after that instruction.
 * 
 * 
 * @author Andre Mis
 */
public class RawControlFlowGraph extends ControlFlowGraph<BytecodeInstruction> {

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
			if (v.getInstructionId() == instructionId) {
				return v;
			}
		}
		return null;
	}

	@Override
	public BytecodeInstruction getBranch(int branchId) {
		for (BytecodeInstruction v : vertexSet()) {
			if (v.isBranch() && v.getControlDependentBranchId() == branchId) {
				return v;
			}
		}
		return null;
	}

	protected ControlFlowEdge addEdge(BytecodeInstruction src,
			BytecodeInstruction target, boolean isExceptionEdge) {

		ControlFlowEdge e = new ControlFlowEdge(isExceptionEdge);
		if (src.isBranch()) { // TODO handle switches?
			e.setBranchInstruction(src);
			// TODO unsafe, make better!
			e.setBranchExpressionValue(!isNonJumpingEdge(src, target));
		} else if (src.isSwitch()) {

			if (!target.isLabel())
				throw new IllegalStateException(
						"expect control flow edges from switch statements to always target labelNodes");

			LabelNode label = (LabelNode) target.getASMNode();
			if (label == null)
				throw new IllegalStateException(
						"expect BranchPool to contain a Branch for each switch-case-label");

			Branch branchForSwitchCase = BranchPool.getBranchForLabel(label);

			// TODO there is an inconsistency when it comes to switches with
			// empty case: blocks. they do not have their own label, so there
			// can be multiple ControlFlowEdges from the SWITCH instruction to
			// one LabelNode.
			// But currently our RawCFG does not permit multiple edges between
			// two nodes

			if (branchForSwitchCase != null) {
				// throw new
				// IllegalStateException("expect BranchPool to know each switch-case-label");

				// TODO the default: case for each switch is not handled so far,
				// which can lead to the branchForSwitchCase to be null

				e.setBranchInstruction(branchForSwitchCase);
				e.setBranchExpressionValue(true);
			}

		}

		if (!super.addEdge(src, target, e)) {
			logger.debug("edge from " + src.toString() + " to "
					+ target.toString() + " already contained in graph");
			e = super.getEdge(src, target);
			if (e == null)
				throw new IllegalStateException("completely unexpected");
		}

		return e;
	}

	private boolean isNonJumpingEdge(BytecodeInstruction src, // TODO move to
			// ControlFlowGraph
			// and implement
			// analog method
			// in ActualCFG
			BytecodeInstruction dst) {

		return Math.abs(src.getInstructionId() - dst.getInstructionId()) == 1;
	}

	// functionality used to create ActualControlFlowGraph

	public BasicBlock determineBasicBlockFor(BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");

		// TODO clean this up

		logger.debug("creating basic block for " + instruction.toString());

		List<BytecodeInstruction> blockNodes = new ArrayList<BytecodeInstruction>();
		blockNodes.add(instruction);

		Set<BytecodeInstruction> handledChildren = new HashSet<BytecodeInstruction>();
		Set<BytecodeInstruction> handledParents = new HashSet<BytecodeInstruction>();

		Queue<BytecodeInstruction> queue = new LinkedList<BytecodeInstruction>();
		queue.add(instruction);
		while (!queue.isEmpty()) {
			BytecodeInstruction current = queue.poll();
			logger.debug("handling " + current.toString());

			// add child to queue
			if (outDegreeOf(current) == 1)
				for (BytecodeInstruction child : getChildren(current)) {
					// this must be only one edge if inDegree was 1

					if (blockNodes.contains(child))
						continue;

					if (handledChildren.contains(child))
						continue;
					handledChildren.add(child);

					if (inDegreeOf(child) < 2) {
						// insert child right after current
						// ... always thought ArrayList had insertBefore() and
						// insertAfter() methods ... well
						blockNodes.add(blockNodes.indexOf(current) + 1, child);

						logger.debug("  added child to queue: "
								+ child.toString());
						queue.add(child);
					}
				}

			// add parent to queue
			if (inDegreeOf(current) == 1)
				for (BytecodeInstruction parent : getParents(current)) {
					// this must be only one edge if outDegree was 1

					if (blockNodes.contains(parent))
						continue;

					if (handledParents.contains(parent))
						continue;
					handledParents.add(parent);

					if (outDegreeOf(parent) < 2) {
						// insert parent right before current
						blockNodes.add(blockNodes.indexOf(current), parent);

						logger.debug("  added parent to queue: "
								+ parent.toString());
						queue.add(parent);
					}
				}
		}

		BasicBlock r = new BasicBlock(className, methodName, blockNodes);

		logger.debug("created nodeBlock: " + r.toString());
		return r;
	}

	@Override
	protected BytecodeInstruction determineEntryPoint() {

		BytecodeInstruction noParent = super.determineEntryPoint();
		if (noParent != null)
			return noParent;

		// copied from ControlFlowGraph.determineEntryPoint():
		// there was a back loop to the first instruction within this CFG, so no
		// candidate
		// TODO for now return null and handle in super class
		// RawControlFlowGraph separately by overriding this method

		return getInstructionWithSmallestId();
	}

	public BytecodeInstruction getInstructionWithSmallestId() {

		BytecodeInstruction r = null;

		for (BytecodeInstruction ins : vertexSet()) {
			if (r == null || r.getInstructionId() > ins.getInstructionId())
				r = ins;
		}

		return r;
	}

	// control distance functionality

	/**
	 * Returns the number of byteCode instructions that can potentially be
	 * executed from entering the method of this CFG until the given CFGVertex
	 * is reached.
	 */
	public Set<BytecodeInstruction> getPreviousInstructionsInMethod(
			BytecodeInstruction v) {
		Set<BytecodeInstruction> visited = new HashSet<BytecodeInstruction>();
		PriorityQueue<BytecodeInstruction> queue = new PriorityQueue<BytecodeInstruction>(
				graph.vertexSet().size(), new BytecodeInstructionIdComparator());
		queue.add(v);
		while (queue.peek() != null) {
			BytecodeInstruction current = queue.poll();
			if (visited.contains(current))
				continue;
			Set<ControlFlowEdge> incomingEdges = graph.incomingEdgesOf(current);
			for (ControlFlowEdge incomingEdge : incomingEdges) {
				BytecodeInstruction source = graph.getEdgeSource(incomingEdge);
				if (source.getInstructionId() >= current.getInstructionId())
					continue;
				queue.add(source);
			}
			visited.add(current);
		}
		return visited;
	}

	/**
	 * Returns the number of byteCode instructions that can potentially be
	 * executed from passing the given CFVertex until the end of the method of
	 * this CFG is reached.
	 */
	@SuppressWarnings("unchecked")
	public Set<BytecodeInstruction> getLaterInstructionsInMethod(
			BytecodeInstruction v) {
		Set<BytecodeInstruction> visited = new HashSet<BytecodeInstruction>();
		Comparator<BytecodeInstruction> reverseComp = new ReverseComparator(
				new BytecodeInstructionIdComparator());
		PriorityQueue<BytecodeInstruction> queue = new PriorityQueue<BytecodeInstruction>(
				graph.vertexSet().size(), reverseComp);
		queue.add(v);
		while (queue.peek() != null) {
			BytecodeInstruction current = queue.poll();
			if (visited.contains(current))
				continue;
			Set<ControlFlowEdge> outgoingEdges = graph.outgoingEdgesOf(current);
			for (ControlFlowEdge outgoingEdge : outgoingEdges) {
				BytecodeInstruction target = graph.getEdgeTarget(outgoingEdge);
				if (target.getInstructionId() < current.getInstructionId())
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

	private Set<Use> getUsesForDef(Definition targetDef,
			BytecodeInstruction entry) {
		if (!graph.containsVertex(entry))
			throw new IllegalArgumentException("vertex not in graph");

		Set<Use> r = new HashSet<Use>();

		Set<ControlFlowEdge> outgoingEdges = graph.outgoingEdgesOf(entry);
		for (ControlFlowEdge e : outgoingEdges) {
			BytecodeInstruction edgeTarget = graph.getEdgeTarget(e);

			if (edgeTarget.isDefUse()) {
				if (targetDef.canBeActiveFor(edgeTarget))
					r.add(DefUseFactory.makeUse(edgeTarget));
				if (targetDef.canBecomeActiveDefinition(edgeTarget))
					continue;
			}
			if (edgeTarget.getInstructionId() > entry.getInstructionId()) // dont
				// follow
				// backedges
				// (loops)
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

	private boolean hasDefClearPathToMethodExit(DefUse targetDefUse,
			BytecodeInstruction currentVertex) {
		if (!graph.containsVertex(currentVertex))
			throw new IllegalArgumentException("vertex not in graph");

		// TODO corner case when this method is initially called with a
		// definition?
		// .. which should never happen cause this method is meant to be called
		// for uses ...
		// TODO make this explicit

		Set<ControlFlowEdge> outgoingEdges = graph
				.outgoingEdgesOf(currentVertex);
		if (outgoingEdges.size() == 0)
			return true;

		for (ControlFlowEdge e : outgoingEdges) {
			BytecodeInstruction edgeTarget = graph.getEdgeTarget(e);

			// skip edges going into another def for the same field
			if (targetDefUse.canBecomeActiveDefinition(edgeTarget))
				continue;

			if (edgeTarget.getInstructionId() > currentVertex
					.getInstructionId() // dont follow backedges (loops)
					&& hasDefClearPathToMethodExit(targetDefUse, edgeTarget))
				return true;
		}
		return false;
	}

	private boolean hasDefClearPathFromMethodEntry(DefUse targetDefUse,
			BytecodeInstruction currentVertex) {
		if (!graph.containsVertex(currentVertex))
			throw new IllegalArgumentException("vertex not in graph");

		Set<ControlFlowEdge> incomingEdges = graph
				.incomingEdgesOf(currentVertex);
		if (incomingEdges.size() == 0)
			return true;

		for (ControlFlowEdge e : incomingEdges) {
			BytecodeInstruction edgeStart = graph.getEdgeSource(e);

			// skip edges coming from a def for the same field
			if (targetDefUse.canBecomeActiveDefinition(edgeStart))
				continue;

			if (edgeStart.getInstructionId() < currentVertex.getInstructionId() // dont
					// follow
					// backedges
					// (loops)
					&& hasDefClearPathFromMethodEntry(targetDefUse, edgeStart))
				return true;
		}
		return false;
	}

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
		return "RawCFG" + graphId + "_" + methodName; // TODO make nice
	}
}
