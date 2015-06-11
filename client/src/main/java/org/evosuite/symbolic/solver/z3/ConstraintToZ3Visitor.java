package org.evosuite.symbolic.solver.z3;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.ConstraintVisitor;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.expr.bv.IntegerConstant;
import org.evosuite.symbolic.expr.bv.StringComparison;

class ConstraintToZ3Visitor implements ConstraintVisitor<String, Void> {

	public ConstraintToZ3Visitor() {
	}

	@Override
	public String visit(IntegerConstraint c, Void arg) {
		ExprToZ3Visitor v = new ExprToZ3Visitor();

		String left = c.getLeftOperand().accept(v, null);
		String right = c.getRightOperand().accept(v, null);

		if (left == null || right == null) {
			return null;
		}

		Comparator cmp = c.getComparator();
		return mkArithmeticComparison(left, cmp, right);
	}

	private String mkArithmeticComparison(String left_arith_expr,
			Comparator cmp, String right_arith_expr) {
		switch (cmp) {
		case LT: {
			String lt = Z3ExprBuilder.mkLt(left_arith_expr, right_arith_expr);
			return lt;
		}
		case LE: {
			String le = Z3ExprBuilder.mkLe(left_arith_expr, right_arith_expr);
			return le;
		}
		case GT: {
			String gt = Z3ExprBuilder.mkGt(left_arith_expr, right_arith_expr);
			return gt;
		}
		case GE: {
			String ge = Z3ExprBuilder.mkGe(left_arith_expr, right_arith_expr);
			return ge;
		}
		case EQ: {
			String ge = Z3ExprBuilder.mkEq(left_arith_expr, right_arith_expr);
			return ge;
		}
		case NE: {
			String ge = Z3ExprBuilder.mkEq(left_arith_expr, right_arith_expr);
			String ne = Z3ExprBuilder.mkNot(ge);
			return ne;
		}
		default: {
			throw new RuntimeException("Unknown comparator for constraint "
					+ cmp.toString());
		}
		}
	}

	@Override
	public String visit(RealConstraint c, Void arg) {
		ExprToZ3Visitor v = new ExprToZ3Visitor();

		String left = c.getLeftOperand().accept(v, null);
		String right = c.getRightOperand().accept(v, null);

		if (left == null || right == null) {
			return null;
		}

		Comparator cmp = c.getComparator();
		String boolExpr = mkArithmeticComparison(left, cmp, right);
		return boolExpr;
	}

	@Override
	public String visit(StringConstraint c, Void arg) {
		ExprToZ3Visitor v = new ExprToZ3Visitor();

		StringComparison stringComparison = (StringComparison) c
				.getLeftOperand();

		IntegerConstant integerConstant = (IntegerConstant) c.getRightOperand();

		String left = stringComparison.accept(v, null);
		String right = integerConstant.accept(v, null);

		if (left == null || right == null) {
			return null;
		}

		Comparator cmp = c.getComparator();
		return mkArithmeticComparison(left, cmp, right);
	}
}
