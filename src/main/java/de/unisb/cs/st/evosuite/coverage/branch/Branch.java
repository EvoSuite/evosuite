package de.unisb.cs.st.evosuite.coverage.branch;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;

/**
 * An object of this class corresponds to a Branch inside the class under test.
 * 
 * Branches are created by the CFGMethodAdapter via the BranchPool. Each Branch
 * holds its corresponding CFGVertex from the ControlFlowGraph.
 * 
 * @author Andre Mis
 */
public class Branch extends BytecodeInstruction {

	private int actualBranchId;

	public Branch(BytecodeInstruction wrapper) {
		super(wrapper);
		if (!isActualBranch()) {
			throw new IllegalArgumentException("only actual branch instructions are accepted");
		}

		if (!BranchPool.isKnownAsBranch(wrapper)) {
			throw new IllegalArgumentException(
					"expect Branch(BytecodeInstruction) constructor to be called only for instruction already known to the BranchPool");
		}

		this.actualBranchId = BranchPool.getActualBranchIdForInstruction(wrapper);

		if (this.actualBranchId < 1) {
			throw new IllegalStateException("expect branch to have actualBranchId set to positive value");
		}
	}

	public Branch(BytecodeInstruction wrapper, int actualBranchId) {
		super(wrapper);
		if (!isActualBranch()) {
			throw new IllegalArgumentException("only actual branch instructions are accepted");
		}

		this.actualBranchId = actualBranchId;

		if (this.actualBranchId < 1) {
			throw new IllegalStateException("expect branch to have actualBranchId set to positive value");
		}
	}

	public int getActualBranchId() {
		return actualBranchId;
	}
}
