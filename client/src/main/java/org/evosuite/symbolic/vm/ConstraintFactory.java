package org.evosuite.symbolic.vm;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.bv.IntegerValue;

/**
 * 
 * @author galeotti
 *
 */
public abstract class ConstraintFactory {

	public static IntegerConstraint eq(IntegerValue left,
			IntegerValue right) {
		return new IntegerConstraint(left, Comparator.EQ, right);
	}

	public static IntegerConstraint neq(IntegerValue left,
			IntegerValue right) {
		return new IntegerConstraint(left, Comparator.NE, right);
	}

	public static IntegerConstraint lt(IntegerValue left,
			IntegerValue right) {
		return new IntegerConstraint(left, Comparator.LT, right);

	}

	public static IntegerConstraint gte(IntegerValue left,
			IntegerValue right) {
		return new IntegerConstraint(left, Comparator.GE, right);

	}
}
