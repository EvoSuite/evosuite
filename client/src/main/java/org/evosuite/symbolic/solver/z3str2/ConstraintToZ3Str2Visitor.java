package org.evosuite.symbolic.solver.z3str2;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.ConstraintVisitor;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.solver.SmtExprBuilder;
import org.evosuite.symbolic.solver.smt.SmtExpr;

class ConstraintToZ3Str2Visitor implements ConstraintVisitor<SmtExpr, Void> {

	public ConstraintToZ3Str2Visitor() {
	}

	@Override
	public SmtExpr visit(IntegerConstraint c, Void arg) {
		ExprToZ3Str2Visitor v = new ExprToZ3Str2Visitor();

		SmtExpr left = c.getLeftOperand().accept(v, null);
		SmtExpr right = c.getRightOperand().accept(v, null);

		if (left == null || right == null) {
			return null;
		}

		Comparator cmp = c.getComparator();

		return mkComparison(left, cmp, right);
	}

	@Override
	public SmtExpr visit(RealConstraint c, Void arg) {
		ExprToZ3Str2Visitor v = new ExprToZ3Str2Visitor();

		SmtExpr left = c.getLeftOperand().accept(v, null);
		SmtExpr right = c.getRightOperand().accept(v, null);

		if (left == null || right == null) {
			return null;
		}

		Comparator cmp = c.getComparator();

		return mkComparison(left, cmp, right);
	}

	@Override
	public SmtExpr visit(StringConstraint c, Void arg) {
		ExprToZ3Str2Visitor v = new ExprToZ3Str2Visitor();

		SmtExpr left = c.getLeftOperand().accept(v, null);
		SmtExpr right = c.getRightOperand().accept(v, null);

		if (left == null || right == null) {
			return null;
		}

		Comparator cmp = c.getComparator();
		return mkComparison(left, cmp, right);
	}

	private static SmtExpr mkComparison(SmtExpr left, Comparator cmp,
			SmtExpr right) {
		switch (cmp) {
		case LT: {
			SmtExpr lt = SmtExprBuilder.mkLt(left, right);
			return lt;
		}
		case LE: {
			SmtExpr le = SmtExprBuilder.mkLe(left, right);
			return le;
		}
		case GT: {
			SmtExpr gt = SmtExprBuilder.mkGt(left, right);
			return gt;
		}
		case GE: {
			SmtExpr ge = SmtExprBuilder.mkGe(left, right);
			return ge;
		}
		case EQ: {
			SmtExpr ge = SmtExprBuilder.mkEq(left, right);
			return ge;
		}
		case NE: {
			SmtExpr ge = SmtExprBuilder.mkEq(left, right);
			SmtExpr ne = SmtExprBuilder.mkNot(ge);
			return ne;
		}
		default: {
			throw new RuntimeException("Unknown comparator for constraint "
					+ cmp.toString());
		}
		}
	}
}
