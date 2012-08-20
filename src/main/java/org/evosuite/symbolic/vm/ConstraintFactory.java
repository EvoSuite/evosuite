package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.IntegerExpression;

/**
 * 
 * @author galeotti
 *
 */
public abstract class ConstraintFactory {

	public static IntegerConstraint eq(IntegerExpression left,
			IntegerExpression right) {
		return new IntegerConstraint(left, Comparator.EQ, right);
	}

	public static IntegerConstraint neq(IntegerExpression left,
			IntegerExpression right) {
		return new IntegerConstraint(left, Comparator.NE, right);
	}

	public static IntegerConstraint lt(IntegerExpression left,
			IntegerExpression right) {
		return new IntegerConstraint(left, Comparator.LT, right);

	}

	public static IntegerConstraint gte(IntegerExpression left,
			IntegerExpression right) {
		return new IntegerConstraint(left, Comparator.GE, right);

	}
}
