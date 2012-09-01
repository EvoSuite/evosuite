package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class SINH extends MathFunction_D2D {

	private static final String SINH = "sinh";

	public SINH(SymbolicEnvironment env) {
		super(env, SINH);
	}

	@Override
	protected RealValue executeFunction(double res) {
		Operator op = Operator.SINH;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
