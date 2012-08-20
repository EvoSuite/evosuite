package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class HYPOT extends MathFunction_DD2D {

	private static final String HYPOT = "hypot";

	public HYPOT(SymbolicEnvironment env) {
		super(env, HYPOT);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.HYPOT;
		return new RealBinaryExpression(left, op, right, res);
	}

}
