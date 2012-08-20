package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class IEEEremainder extends MathFunction_DD2D {

	private static final String IEEE_REMAINDER = "IEEEremainder";

	public IEEEremainder(SymbolicEnvironment env) {
		super(env, IEEE_REMAINDER);
	}

	@Override
	protected RealExpression executeFunction(double res) {
		Operator op = Operator.IEEEREMAINDER;
		return new RealBinaryExpression(left, op, right, res);

	}

}
