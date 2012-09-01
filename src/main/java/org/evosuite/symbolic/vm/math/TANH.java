package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class TANH extends MathFunction_D2D {

	private static final String TANH = "tanh";

	public TANH(SymbolicEnvironment env) {
		super(env, TANH);
	}

	@Override
	protected RealValue executeFunction(double res) {
		Operator op = Operator.TANH;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
