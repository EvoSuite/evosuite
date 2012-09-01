package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class ATAN2 extends MathFunction_DD2D {

	private static final String ATAN2 = "atan2";

	public ATAN2(SymbolicEnvironment env) {
		super(env, ATAN2);
	}

	@Override
	protected RealValue executeFunction(double res) {
		Operator op = Operator.ATAN2;
		return new RealBinaryExpression(left, op, right, res);
	}

}
