package de.unisb.cs.st.evosuite.cfg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultEdge;

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
		EvoSuiteGraph<BytecodeInstruction, DefaultEdge> {

	private static Logger logger = Logger.getLogger(RawControlFlowGraph.class);

	
	private String className;
	private String methodName;
	
	public RawControlFlowGraph(String className, String methodName) {
		super(DefaultEdge.class);
		
		if (className == null || methodName == null)
			throw new IllegalArgumentException("null given");
		
		this.className = className;
		this.methodName = methodName;
	}

	public BasicBlock determineBasicBlockFor(BytecodeInstruction instruction) {
		if (instruction == null)
			throw new IllegalArgumentException("null given");

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
				for (DefaultEdge edge : incomingEdgesOf(current)) {
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
				for (DefaultEdge edge : outgoingEdgesOf(current)) {
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
	
	public BytecodeInstruction determineEntryPoint() {
		BytecodeInstruction r = null;

		for (BytecodeInstruction instruction : vertexSet())
			if (inDegreeOf(instruction) == 0) {
				if (r != null)
					throw new IllegalStateException(
							"expect raw CFG of a method to contain exactly one instruction with no parent");
				r = instruction;
			}
		if (r == null)
			throw new IllegalStateException(
					"expect raw CFG of a method to contain exactly one instruction with no parent");

		return r;
	}

	public Set<BytecodeInstruction> determineExitPoints() {
		Set<BytecodeInstruction> r = new HashSet<BytecodeInstruction>();

		for (BytecodeInstruction instruction : vertexSet())
			if (outDegreeOf(instruction) == 0) {
				r.add(instruction);
			}
		if (r.isEmpty())
			throw new IllegalStateException(
					"expect raw CFG of a method to contain at least one instruction with no child");

		return r;
	}	
	
	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}
}
