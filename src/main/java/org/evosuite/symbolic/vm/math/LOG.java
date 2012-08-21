package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class LOG extends MathFunction_D2D {

	private static final String LOG = "log";

	public LOG(SymbolicEnvironment env) {
		super(env, LOG);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.LOG;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
