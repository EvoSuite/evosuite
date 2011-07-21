package de.unisb.cs.st.evosuite.coverage.branch;

import de.unisb.cs.st.evosuite.cfg.BytecodeInstruction;

/**
 * An object of this class corresponds to a Branch inside the class under test.
 * 
 * Branches are created by the CFGMethodAdapter via the BranchPool. Each Branch
 * holds its corresponding CFGVertex from the ControlFlowGraph.
 * 
 * For SWITCH statements each case <key>: block corresponds to a Branch that can
 * be created by constructing a Branch with the SWITCH statement and the <key>
 * as the targetCaseValue
 * 
 * @author Andre Mis
 */
public class Branch extends BytecodeInstruction {

	private final int actualBranchId;

	private Integer targetCaseValue = null;

	/**
	 * Constructor for usual jump instruction Branches, that are not SWITCH
	 * instructions.
	 */
	public Branch(BytecodeInstruction wrapper, int actualBranchId) {
		super(wrapper);
		if (!isBranch())
			throw new IllegalArgumentException(
					"only branch instructions are accepted");

		this.actualBranchId = actualBranchId;

		if (this.actualBranchId < 1)
			throw new IllegalStateException(
					"expect branch to have actualBranchId set to positive value");
	}

	public Branch(BytecodeInstruction wrapper) {
		super(wrapper);
		if (!isBranch())
			throw new IllegalArgumentException(
					"only branch instructions are accepted");

		if (!BranchPool.isKnownAsBranch(wrapper))
			throw new IllegalArgumentException(
					"expect Branch(BytecodeInstruction) constructor to be called only for instruction already known to the BranchPool");

		this.actualBranchId = BranchPool
				.getActualBranchIdForInstruction(wrapper);

		if (this.actualBranchId < 1)
			throw new IllegalStateException(
					"expect branch to have actualBranchId set to positive value");
	}

	/**
	 * Constructor for SWITCH-Case: Branches
	 * 
	 */
	public Branch(BytecodeInstruction switchWrapper, int targetCaseValue,
			int actualBranchId) {
		super(switchWrapper);
		if (!isSwitch())
			throw new IllegalArgumentException("switch instruction expected");

		this.actualBranchId = actualBranchId;

		this.targetCaseValue = targetCaseValue;

		if (this.actualBranchId < 1)
			throw new IllegalStateException(
					"expect branch to have actualBranchId set to positive value");
	}

	public int getActualBranchId() {
		return actualBranchId;
	}

	public Integer getTargetCaseValue() {
		return targetCaseValue;
	}

	@Override
	public String toString() {
		String r = "I" + getInstructionId();
		r += " Branch " + getActualBranchId();
		r += " " + getInstructionType();
		if (targetCaseValue != null)
			r += " Case " + targetCaseValue;

		r += " L" + getLineNumber();

		return r;
	}
}
