package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class ATAN extends MathFunction_D2D {

	private static final String ATAN = "atan";

	public ATAN(SymbolicEnvironment env) {
		super(env, ATAN);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.ATAN;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
