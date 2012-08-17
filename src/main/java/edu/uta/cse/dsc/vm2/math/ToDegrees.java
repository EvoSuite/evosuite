package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class ToDegrees extends MathFunction_D2D {

	private static final String TO_DEGREES = "toDegrees";

	public ToDegrees(SymbolicEnvironment env) {
		super(env, TO_DEGREES);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.TODEGREES;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
