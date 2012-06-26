/**
 * Copyright (C) 2011,2012 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Public License for more details.
 *
 * You should have received a copy of the GNU Public License along with
 * EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package org.evosuite.cfg.instrumentation;

import java.util.LinkedList;
import java.util.Queue;

import org.evosuite.coverage.path.PrimePath;
import org.evosuite.coverage.path.PrimePathPool;
import org.evosuite.graphs.GraphPool;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.ControlFlowEdge;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Gordon Fraser
 * 
 */
public class PrimePathInstrumentation implements MethodInstrumentation {

	protected static Logger logger = LoggerFactory.getLogger(PrimePathInstrumentation.class);

	/* (non-Javadoc)
	 * @see org.evosuite.cfg.MethodInstrumentation#analyze(org.objectweb.asm.tree.MethodNode, org.jgrapht.Graph, java.lang.String, java.lang.String, int)
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
	 * @see org.evosuite.cfg.MethodInstrumentation#executeOnMainMethod()
	 */
	@Override
	public boolean executeOnMainMethod() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.evosuite.cfg.MethodInstrumentation#executeOnExcludedMethods()
	 */
	@Override
	public boolean executeOnExcludedMethods() {
		// TODO Auto-generated method stub
		return false;
	}

}
