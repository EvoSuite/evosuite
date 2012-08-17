package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class EXPM1 extends MathFunction_D2D {

	private static final String EXPM1 = "expm1";

	public EXPM1(SymbolicEnvironment env) {
		super(env, EXPM1);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.EXPM1;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
