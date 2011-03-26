package de.unisb.cs.st.evosuite.cfg;

import java.util.Comparator;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;

/**
 * Orders CFGVertices according to their id
 * 
 * This is mainly used to put CFGVertex into a PriorityQueue
 * in ControlFlowGraph.getMaximalInitialDistance()
 */
public class CFGVertexIdComparator implements Comparator<CFGVertex> {

	@Override
	public int compare(CFGVertex arg0, CFGVertex arg1) {

		return new Integer(arg0.getId()).compareTo(arg1.getId());
	}
}
