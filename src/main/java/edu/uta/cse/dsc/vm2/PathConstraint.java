package edu.uta.cse.dsc.vm2;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstraint;

public final class PathConstraint {

	private Stack<BranchCondition> branchConditions = new Stack<BranchCondition>();

	private LinkedList<Constraint<?>> currentLocalConstraints = new LinkedList<Constraint<?>>();
	private LinkedList<Constraint<?>> reachingConstraints = new LinkedList<Constraint<?>>();

	public void pushLocalConstraint(IntegerConstraint c) {
		currentLocalConstraints.add(c);
	}

	public void pushBranchCondition(String className, String methName,
			int branchIndex, IntegerConstraint ending_constraint) {
		this.pushLocalConstraint(ending_constraint);

		HashSet<Constraint<?>> branch_reaching_constraints = new HashSet<Constraint<?>>(
				reachingConstraints);
		LinkedList<Constraint<?>> branch_local_constraints = new LinkedList<Constraint<?>>(
				currentLocalConstraints);

		BranchCondition new_branch = new BranchCondition(methName, methName,
				branchIndex, branch_reaching_constraints,
				branch_local_constraints);

		branchConditions.push(new_branch);

		reachingConstraints.addAll(currentLocalConstraints);
		currentLocalConstraints.clear();
	}

	public List<BranchCondition> getBranchConditions() {
		return new LinkedList<BranchCondition>(branchConditions);
	}

}
