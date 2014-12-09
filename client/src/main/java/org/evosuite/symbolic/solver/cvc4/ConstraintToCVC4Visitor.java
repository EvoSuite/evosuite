package org.evosuite.symbolic.solver.cvc4;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.ConstraintVisitor;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.solver.smt.SmtExpr;

class ConstraintToCVC4Visitor implements ConstraintVisitor<SmtExpr, Void> {

	private final Set<String> stringConstants = new HashSet<String>();

	private final ExprToCVC4Visitor exprVisitor = new ExprToCVC4Visitor();

	@Override
	public SmtExpr visit(IntegerConstraint c, Void arg) {
		Expression<?> leftOperand = c.getLeftOperand();
		Expression<?> rightOperand = c.getRightOperand();
		Comparator cmp = c.getComparator();
		return visit(leftOperand, cmp, rightOperand);
	}

	private SmtExpr visit(Expression<?> leftOperand, Comparator cmp,
			Expression<?> rightOperand) {
		SmtExpr left = leftOperand.accept(exprVisitor, null);
		SmtExpr right = rightOperand.accept(exprVisitor, null);

		if (left == null || right == null) {
			return null;
		}

		return mkComparison(left, cmp, right);
	}

	@Override
	public SmtExpr visit(RealConstraint c, Void arg) {
		Expression<?> leftOperand = c.getLeftOperand();
		Expression<?> rightOperand = c.getRightOperand();
		Comparator cmp = c.getComparator();
		return visit(leftOperand, cmp, rightOperand);
	}

	@Override
	public SmtExpr visit(StringConstraint c, Void arg) {
		Expression<?> leftOperand = c.getLeftOperand();
		Expression<?> rightOperand = c.getRightOperand();
		Comparator cmp = c.getComparator();
		return visit(leftOperand, cmp, rightOperand);
	}

	private static SmtExpr mkComparison(SmtExpr left, Comparator cmp,
			SmtExpr right) {
		switch (cmp) {
		case LT: {
			SmtExpr lt = CVC4ExprBuilder.mkLt(left, right);
			return lt;
		}
		case LE: {
			SmtExpr le = CVC4ExprBuilder.mkLe(left, right);
			return le;
		}
		case GT: {
			SmtExpr gt = CVC4ExprBuilder.mkGt(left, right);
			return gt;
		}
		case GE: {
			SmtExpr ge = CVC4ExprBuilder.mkGe(left, right);
			return ge;
		}
		case EQ: {
			SmtExpr ge = CVC4ExprBuilder.mkEq(left, right);
			return ge;
		}
		case NE: {
			SmtExpr ge = CVC4ExprBuilder.mkEq(left, right);
			SmtExpr ne = CVC4ExprBuilder.mkNot(ge);
			return ne;
		}
		default: {
			throw new RuntimeException("Unknown comparator for constraint "
					+ cmp.toString());
		}
		}
	}

	public Set<String> getStringConstants() {
		return stringConstants;
	}
}
