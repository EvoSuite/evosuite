package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class RINT extends MathFunction_D2D {

	private static final String RINT = "rint";

	public RINT(SymbolicEnvironment env) {
		super(env, RINT);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.RINT;
		return new RealUnaryExpression(realExpression, op, res);
	}

}
