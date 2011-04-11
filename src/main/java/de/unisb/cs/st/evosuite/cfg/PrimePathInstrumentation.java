/**
 * 
 */
package de.unisb.cs.st.evosuite.cfg;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.objectweb.asm.tree.MethodNode;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.path.PrimePath;
import de.unisb.cs.st.evosuite.coverage.path.PrimePathPool;

/**
 * @author Gordon Fraser
 * 
 */
public class PrimePathInstrumentation implements MethodInstrumentation {

	protected static Logger logger = Logger.getLogger(PrimePathInstrumentation.class);

	/* (non-Javadoc)
	 * @see de.unisb.cs.st.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String, java.lang.String, int)
	 */
	@Override
	public void analyze(MethodNode mn, Graph<CFGVertex, DefaultEdge> g, String className,
	        String methodName, int access) {
		DefaultDirectedGraph<CFGVertex, DefaultEdge> graph = (DefaultDirectedGraph<CFGVertex, DefaultEdge>) g;
		Queue<PrimePath> path_queue = new LinkedList<PrimePath>();
		for (CFGVertex vertex : graph.vertexSet()) {
			if (graph.inDegreeOf(vertex) == 0) {
				PrimePath initial = new PrimePath(className, methodName);
				initial.append(vertex);
				path_queue.add(initial);
			}
		}
		while (!path_queue.isEmpty()) {
			PrimePath current = path_queue.poll();
			for (DefaultEdge edge : graph.outgoingEdgesOf(current.getLast())) {
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
