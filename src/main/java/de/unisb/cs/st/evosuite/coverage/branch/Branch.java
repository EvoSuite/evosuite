package de.unisb.cs.st.evosuite.coverage.branch;

import de.unisb.cs.st.evosuite.cfg.CFGGenerator.CFGVertex;
import de.unisb.cs.st.evosuite.coverage.CFGVertexHolder;

/**
 * An object of this class corresponds to a Branch inside the class under test.
 * 
 * Branches are created by the CFGMethodAdapter via the BranchPool. Each Branch
 * holds its corresponding CFGVertex from the ControlFlowGraph.
 * 
 * @author Andre Mis
 */
public class Branch extends CFGVertexHolder {

	public Branch(CFGVertex v) {
		if (!(v.isBranch() || v.isLookupSwitch() || v.isTableSwitch()))
			throw new IllegalArgumentException("Vertex of a branch expected");

		this.v = v;
	}

}
