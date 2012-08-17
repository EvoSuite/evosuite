package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class CEIL extends MathFunction_D2D {

	private static final String CEIL = "ceil";

	public CEIL(SymbolicEnvironment env) {
		super(env, CEIL);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.CEIL;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
