package org.evosuite.symbolic.solver.cvc4;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.ConstraintVisitor;
import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.StringConstraint;
import org.evosuite.symbolic.solver.SmtLibExprBuilder;

class ConstraintToCVC4Visitor implements ConstraintVisitor<String, Void> {

	private final Set<String> stringConstants = new HashSet<String>();

	private final ExprToCVC4Visitor exprVisitor = new ExprToCVC4Visitor();

	@Override
	public String visit(IntegerConstraint c, Void arg) {
		Expression<?> leftOperand = c.getLeftOperand();
		Expression<?> rightOperand = c.getRightOperand();
		Comparator cmp = c.getComparator();
		return visit(leftOperand, cmp, rightOperand);
	}

	private String visit(Expression<?> leftOperand, Comparator cmp,
			Expression<?> rightOperand) {
		String left = leftOperand.accept(exprVisitor, null);
		String right = rightOperand.accept(exprVisitor, null);

		if (left == null || right == null) {
			return null;
		}

		return mkComparison(left, cmp, right);
	}

	@Override
	public String visit(RealConstraint c, Void arg) {
		Expression<?> leftOperand = c.getLeftOperand();
		Expression<?> rightOperand = c.getRightOperand();
		Comparator cmp = c.getComparator();
		return visit(leftOperand, cmp, rightOperand);
	}

	@Override
	public String visit(StringConstraint c, Void arg) {
		Expression<?> leftOperand = c.getLeftOperand();
		Expression<?> rightOperand = c.getRightOperand();
		Comparator cmp = c.getComparator();
		return visit(leftOperand, cmp, rightOperand);
	}

	private static String mkComparison(String left, Comparator cmp, String right) {
		switch (cmp) {
		case LT: {
			String lt = SmtLibExprBuilder.mkLt(left, right);
			return lt;
		}
		case LE: {
			String le = SmtLibExprBuilder.mkLe(left, right);
			return le;
		}
		case GT: {
			String gt = SmtLibExprBuilder.mkGt(left, right);
			return gt;
		}
		case GE: {
			String ge = SmtLibExprBuilder.mkGe(left, right);
			return ge;
		}
		case EQ: {
			String ge = SmtLibExprBuilder.mkEq(left, right);
			return ge;
		}
		case NE: {
			String ge = SmtLibExprBuilder.mkEq(left, right);
			String ne = SmtLibExprBuilder.mkNot(ge);
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
