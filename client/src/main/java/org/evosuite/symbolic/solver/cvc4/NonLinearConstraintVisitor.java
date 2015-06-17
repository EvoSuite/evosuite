package org.evosuite.symbolic.solver.cvc4;

import org.evosuite.symbolic.expr.ConstraintVisitor;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.StringConstraint;

final class NonLinearConstraintVisitor implements ConstraintVisitor<Boolean, Void> {

	private final NonLinearExpressionVisitor exprVisitor = new NonLinearExpressionVisitor();

	@Override
	public Boolean visit(IntegerConstraint n, Void arg) {
		Boolean left_ret_val = n.getLeftOperand().accept(exprVisitor, null);
		if (left_ret_val) {
			return true;
		}

		Boolean right_ret_val = n.getRightOperand().accept(exprVisitor, null);
		if (right_ret_val) {
			return right_ret_val;
		}

		return false;
	}

	@Override
	public Boolean visit(RealConstraint n, Void arg) {
		Boolean left_ret_val = n.getLeftOperand().accept(exprVisitor, null);
		if (left_ret_val) {
			return true;
		}

		Boolean right_ret_val = n.getRightOperand().accept(exprVisitor, null);
		if (right_ret_val) {
			return right_ret_val;
		}

		return false;
	}

	@Override
	public Boolean visit(StringConstraint n, Void arg) {
		Boolean left_ret_val = n.getLeftOperand().accept(exprVisitor, null);
		if (left_ret_val) {
			return true;
		}

		Boolean right_ret_val = n.getRightOperand().accept(exprVisitor, null);
		if (right_ret_val) {
			return right_ret_val;
		}

		return false;
	}

}
