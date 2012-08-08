package edu.uta.cse.dsc.vm2;

import org.evosuite.symbolic.expr.Comparator;
import org.evosuite.symbolic.expr.IntegerConstraint;
import org.evosuite.symbolic.expr.IntegerExpression;

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
		// TODO Auto-generated method stub
		return null;
	}

	public static IntegerConstraint gte(IntegerExpression leftBv,
			IntegerExpression rightBv) {
		// TODO Auto-generated method stub
		return null;
	}
}
