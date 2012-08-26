package org.evosuite.symbolic.vm;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.evosuite.symbolic.BranchCondition;
import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.Constraint;
import org.evosuite.symbolic.expr.IntegerConstant;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.StringComparison;
import org.evosuite.symbolic.expr.StringToIntCast;

/**
 * 
 * @author galeotti
 * 
 */
public final class PathConstraint {

	private BranchCondition previousBranchCondition = null;

	private final Stack<BranchCondition> branchConditions = new Stack<BranchCondition>();

	private final LinkedList<Constraint<?>> currentLocalConstraints = new LinkedList<Constraint<?>>();

	public void pushLocalConstraint(IntegerConstraint c) {

		IntegerExpression left_integer_expression = (IntegerExpression) c
				.getLeftOperand();
		IntegerExpression right_integer_expression = (IntegerExpression) c
				.getRightOperand();
		Comparator comp = c.getComparator();

		if (isStringConstraint(left_integer_expression, comp,
				right_integer_expression)) {

			c = createNormalizedIntegerConstraint(left_integer_expression,
					comp, right_integer_expression);
		} else

		if (isStringConstraint(right_integer_expression, comp,
				left_integer_expression)) {
			c = createNormalizedIntegerConstraint(right_integer_expression,
					comp.swap(), left_integer_expression);

		}
		currentLocalConstraints.add(c);
	}

	public void pushBranchCondition(String className, String methName,
			int branchIndex, IntegerConstraint ending_constraint) {

		// no need to add new branch condition
		if (currentLocalConstraints.size() == 0
				&& !ending_constraint.getLeftOperand()
						.containsSymbolicVariable()
				&& !ending_constraint.getRightOperand()
						.containsSymbolicVariable())
			return;

		// add branch condition but do not add concrete constraint
		if (ending_constraint.getLeftOperand().containsSymbolicVariable()
				|| ending_constraint.getRightOperand()
						.containsSymbolicVariable())
			this.pushLocalConstraint(ending_constraint);

		LinkedList<Constraint<?>> branch_local_constraints = new LinkedList<Constraint<?>>(
				currentLocalConstraints);

		BranchCondition new_branch = new BranchCondition(
				previousBranchCondition, methName, methName, branchIndex,
				branch_local_constraints);
		previousBranchCondition = new_branch;

		branchConditions.push(new_branch);

		currentLocalConstraints.clear();
	}

	public List<BranchCondition> getBranchConditions() {
		return new LinkedList<BranchCondition>(branchConditions);
	}

	private IntegerConstraint createNormalizedIntegerConstraint(
			IntegerExpression left, Comparator comp, IntegerExpression right) {
		IntegerConstant integerConstant = (IntegerConstant) right;
		StringComparison stringComparison = (StringComparison) ((StringToIntCast) left)
				.getParam();

		IntegerConstraint c = new IntegerConstraint(stringComparison, comp,
				integerConstant);
		return c;

	}

	private static boolean isStringConstraint(IntegerExpression left,
			Comparator comp, IntegerExpression right) {

		return ((comp.equals(Comparator.NE) || comp.equals(Comparator.EQ))
				&& (right instanceof IntegerConstant)
				&& (left instanceof StringToIntCast) && ((StringToIntCast) left)
					.getParam() instanceof StringComparison);

	}

}
