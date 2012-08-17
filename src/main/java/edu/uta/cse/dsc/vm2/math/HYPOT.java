package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class HYPOT extends MathFunction_DD2D {

	private static final String HYPOT = "hypot";

	public HYPOT(SymbolicEnvironment env) {
		super(env, HYPOT);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.HYPOT;
		return new RealBinaryExpression(left, op, right, res);
	}

}
