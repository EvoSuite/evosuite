package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class CEIL extends MathFunction_D2D {

	private static final String CEIL = "ceil";

	public CEIL(SymbolicEnvironment env) {
		super(env, CEIL);
	}

	@Override
	protected RealValue executeFunction(double res) {
		Operator op = Operator.CEIL;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
