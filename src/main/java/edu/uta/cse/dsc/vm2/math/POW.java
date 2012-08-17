package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class POW extends MathFunction_DD2D {

	private static final String POW = "pow";

	public POW(SymbolicEnvironment env) {
		super(env, POW);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.POW;
		return new RealBinaryExpression(left, op, right, res);
	}

}
