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

	private final LinkedList<Constraint<?>> currentSupportingConstraints = new LinkedList<Constraint<?>>();

	public void pushSupportingConstraint(IntegerConstraint c) {

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
		currentSupportingConstraints.add(c);
	}

	public void pushBranchCondition(String className, String methName,
			int branchIndex, IntegerConstraint local_constraint) {

		LinkedList<Constraint<?>> branch_supporting_constraints = new LinkedList<Constraint<?>>(
				currentSupportingConstraints);

		BranchCondition new_branch = new BranchCondition(
				previousBranchCondition, methName, methName, branchIndex,
				local_constraint, branch_supporting_constraints);

		previousBranchCondition = new_branch;

		branchConditions.push(new_branch);

		currentSupportingConstraints.clear();
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
