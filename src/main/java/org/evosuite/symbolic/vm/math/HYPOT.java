package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class HYPOT extends MathFunction_DD2D {

	private static final String HYPOT = "hypot";

	public HYPOT(SymbolicEnvironment env) {
		super(env, HYPOT);
	}

	@Override
	protected RealValue executeFunction(double res) {
		Operator op = Operator.HYPOT;
		return new RealBinaryExpression(left, op, right, res);
	}

}
