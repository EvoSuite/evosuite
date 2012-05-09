package de.unisb.cs.st.evosuite.graphs.cfg;

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
	
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((branch == null) ? 0 : branch.hashCode());
//		result = prime * result + (branchExpressionValue ? 1231 : 1237);
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		ControlDependency other = (ControlDependency) obj;
//		if (branch == null) {
//			if (other.branch != null)
//				return false;
//		} else if (!branch.equals(other.branch))
//			return false;
//		if (branchExpressionValue != other.branchExpressionValue)
//			return false;
//		return true;
//	}

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
