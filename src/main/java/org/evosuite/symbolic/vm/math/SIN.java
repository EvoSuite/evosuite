package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class SIN extends MathFunction_D2D {

	private static final String SIN = "sin";

	public SIN(SymbolicEnvironment env) {
		super(env, SIN);
	}

	@Override
	protected RealValue executeFunction(double res) {
		Operator op = Operator.SIN;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
