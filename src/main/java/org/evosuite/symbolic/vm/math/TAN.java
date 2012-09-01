package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class TAN extends MathFunction_D2D {

	private static final String TAN = "tan";

	public TAN(SymbolicEnvironment env) {
		super(env, TAN);
	}

	@Override
	protected RealValue executeFunction(double res) {
		Operator op = Operator.TAN;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
