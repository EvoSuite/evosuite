package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class SQRT extends MathFunction_D2D {

	private static final String SQRT = "sqrt";

	public SQRT(SymbolicEnvironment env) {
		super(env, SQRT);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.SQRT;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
