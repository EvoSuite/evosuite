package de.unisb.cs.st.evosuite.cfg;

import java.io.Serializable;

import de.unisb.cs.st.evosuite.coverage.branch.Branch;

public class ControlDependency implements Serializable {

	private static final long serialVersionUID = 6288839964561655730L;

	private final Branch branch;
	private final boolean branchExpressionValue;

	public ControlDependency(Branch branch, boolean branchExpressionValue) {
		if (branch == null)
			throw new IllegalArgumentException(
			        "control dependencies for the root branch are not permitted (null)");

		this.branch = branch;
		this.branchExpressionValue = branchExpressionValue;
	}

	public Branch getBranch() {
		return branch;
	}

	public boolean getBranchExpressionValue() {
		return branchExpressionValue;
	}

	@Override
	public String toString() {

		String r = "CD " + branch.toString();

		if (!branch.isSwitchCaseBranch()) {
			if (branchExpressionValue)
				r += " - TRUE";
			else
				r += " - FALSE";
		}

		return r;
	}
}
