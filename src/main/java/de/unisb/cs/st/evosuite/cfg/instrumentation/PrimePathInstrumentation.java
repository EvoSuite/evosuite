/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg.instrumentation;

import java.util.LinkedList;
import java.util.Queue;

import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unisb.cs.st.evosuite.coverage.path.PrimePath;
import de.unisb.cs.st.evosuite.coverage.path.PrimePathPool;
import de.unisb.cs.st.evosuite.graphs.GraphPool;
import de.unisb.cs.st.evosuite.graphs.cfg.BytecodeInstruction;
import de.unisb.cs.st.evosuite.graphs.cfg.ControlFlowEdge;
import de.unisb.cs.st.evosuite.graphs.cfg.RawControlFlowGraph;

/**
 * @author Gordon Fraser
 * 
 */
public class PrimePathInstrumentation implements MethodInstrumentation {

	protected static Logger logger = LoggerFactory.getLogger(PrimePathInstrumentation.class);

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String, java.lang.String, int)
	 */
	@Override
	public void analyze(MethodNode mn, String className, String methodName, int access) {
		RawControlFlowGraph graph = GraphPool.getRawCFG(className, methodName);
		Queue<PrimePath> path_queue = new LinkedList<PrimePath>();
		for (BytecodeInstruction vertex : graph.vertexSet()) {
			if (graph.inDegreeOf(vertex) == 0) {
				PrimePath initial = new PrimePath(className, methodName);
				initial.append(vertex);
				path_queue.add(initial);
			}
		}
		while (!path_queue.isEmpty()) {
			PrimePath current = path_queue.poll();
			for (ControlFlowEdge edge : graph.outgoingEdgesOf(current.getLast())) {
				if (!current.contains(graph.getEdgeTarget(edge))) {
					PrimePath next = current.getAppended(graph.getEdgeTarget(edge));
					path_queue.add(next);
				}
			}
			if (current.getLast().isReturn() || current.getLast().isThrow()) {
				logger.warn("New path:");
				for (int i = 0; i < current.getSize(); i++) {
					if (current.get(i).isBranch() || current.get(i).isLabel())
						logger.warn(" -> " + current.get(i));
				}
				logger.warn(current.toString());
				PrimePathPool.add(current);
			}
		}
		logger.info("Found " + PrimePathPool.getSize() + " prime paths");

	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#executeOnMainMethod()
	 */
	@Override
	public boolean executeOnMainMethod() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#executeOnExcludedMethods()
	 */
	@Override
	public boolean executeOnExcludedMethods() {
		// TODO Auto-generated method stub
		return false;
	}

}
