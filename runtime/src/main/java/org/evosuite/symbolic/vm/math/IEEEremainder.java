package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class IEEEremainder extends SymbolicFunction {

	private static final String IEEE_REMAINDER = "IEEEremainder";

	public IEEEremainder(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, IEEE_REMAINDER, Types.DD2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue left = this.getSymbRealArgument(0);
		RealValue right = this.getSymbRealArgument(1);
		RealValue ieeeRemainderExpr;
		if (left.containsSymbolicVariable() || right.containsSymbolicVariable()) {
			Operator op = Operator.IEEEREMAINDER;
			ieeeRemainderExpr = new RealBinaryExpression(left, op, right, res);
		} else {
			ieeeRemainderExpr = this.getSymbRealRetVal();
		}
		return ieeeRemainderExpr;
	}
}
