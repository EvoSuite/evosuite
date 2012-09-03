package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class SQRT extends MathFunction_D2D {

	private static final String SQRT = "sqrt";

	public SQRT(SymbolicEnvironment env) {
		super(env, SQRT);
	}

	@Override
	protected RealValue executeFunction(double res) {
		Operator op = Operator.SQRT;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
