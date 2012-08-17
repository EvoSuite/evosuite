package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class FLOOR extends MathFunction_D2D {

	private static final String FLOOR = "floor";

	public FLOOR(SymbolicEnvironment env) {
		super(env, FLOOR);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.FLOOR;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
