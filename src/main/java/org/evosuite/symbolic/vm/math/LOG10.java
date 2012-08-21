package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class LOG10 extends MathFunction_D2D {

	private static final String LOG10 = "log10";

	public LOG10(SymbolicEnvironment env) {
		super(env, LOG10);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.LOG10;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
