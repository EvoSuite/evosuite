package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class COS extends MathFunction_D2D {

	private static final String COS = "cos";

	public COS(SymbolicEnvironment env) {
		super(env, COS);
	}

	@Override
	protected RealValue executeFunction(double res) {
		Operator op = Operator.COS;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
