package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class ATAN2 extends SymbolicFunction {

	private static final String ATAN2 = "atan2";

	public ATAN2(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, ATAN2, Types.DD2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue left = this.getSymbRealArgument(0);
		RealValue right = this.getSymbRealArgument(1);
		RealValue atan2Expr;
		if (left.containsSymbolicVariable() || right.containsSymbolicVariable()) {
			Operator op = Operator.ATAN2;
			atan2Expr = new RealBinaryExpression(left, op, right, res);
		} else {
			atan2Expr = this.getSymbRealRetVal();
		}
		return atan2Expr;
	}
}
