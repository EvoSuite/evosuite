package org.evosuite.symbolic.solver.z3str;

import java.util.HashSet;
import java.util.Set;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.ConstraintVisitor;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.RealConstraint;
import org.evosuite.symbolic.expr.StringConstraint;

class ConstraintToZ3StrVisitor implements ConstraintVisitor<String, Void> {

	private final Set<String> stringConstants =new HashSet<String>();
	
	public ConstraintToZ3StrVisitor() {
	}

	@Override
	public String visit(IntegerConstraint c, Void arg) {
		ExprToZ3StrVisitor v = new ExprToZ3StrVisitor();

		String left = c.getLeftOperand().accept(v, null);
		String right = c.getRightOperand().accept(v, null);

		stringConstants.addAll(v.getStringConstants());
		
		if (left == null || right == null) {
			return null;
		}

		Comparator cmp = c.getComparator();

		return mkComparison(left, cmp, right);
	}

	@Override
	public String visit(RealConstraint c, Void arg) {
		ExprToZ3StrVisitor v = new ExprToZ3StrVisitor();

		String left = c.getLeftOperand().accept(v, null);
		String right = c.getRightOperand().accept(v, null);

		stringConstants.addAll(v.getStringConstants());

		if (left == null || right == null) {
			return null;
		}

		Comparator cmp = c.getComparator();

		return mkComparison(left, cmp, right);
	}

	@Override
	public String visit(StringConstraint c, Void arg) {
		ExprToZ3StrVisitor v = new ExprToZ3StrVisitor();

		String left = c.getLeftOperand().accept(v, null);
		String right = c.getRightOperand().accept(v, null);

		stringConstants.addAll(v.getStringConstants());

		if (left == null || right == null) {
			return null;
		}

		Comparator cmp = c.getComparator();
		return mkComparison(left, cmp, right);
	}

	private String mkComparison(String left, Comparator cmp,
			String right) {
		switch (cmp) {
		case LT: {
			String lt = Z3StrExprBuilder.mkLt(left, right);
			return lt;
		}
		case LE: {
			String le = Z3StrExprBuilder.mkLe(left, right);
			return le;
		}
		case GT: {
			String gt = Z3StrExprBuilder.mkGt(left, right);
			return gt;
		}
		case GE: {
			String ge = Z3StrExprBuilder.mkGe(left, right);
			return ge;
		}
		case EQ: {
			String ge = Z3StrExprBuilder.mkEq(left, right);
			return ge;
		}
		case NE: {
			String ge = Z3StrExprBuilder.mkEq(left, right);
			String ne = Z3StrExprBuilder.mkNot(ge);
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
