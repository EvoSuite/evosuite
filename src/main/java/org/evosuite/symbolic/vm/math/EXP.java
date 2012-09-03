package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class EXP extends MathFunction_D2D {

	private static final String EXP = "exp";

	public EXP(SymbolicEnvironment env) {
		super(env, EXP);
	}

	@Override
	protected RealValue executeFunction(double res) {
		Operator op = Operator.EXP;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
