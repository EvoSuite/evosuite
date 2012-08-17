package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class COS extends MathFunction_D2D {

	private static final String COS = "cos";

	public COS(SymbolicEnvironment env) {
		super(env, COS);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.COS;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
