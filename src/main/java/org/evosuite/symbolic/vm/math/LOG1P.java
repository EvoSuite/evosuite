package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class LOG1P extends SymbolicFunction {

	private static final String LOG1P = "log1p";

	public LOG1P(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, LOG1P, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue log1pExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.LOG1P;
			log1pExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			log1pExpr = this.getSymbRealRetVal();
		}
		return log1pExpr;
	}
}
