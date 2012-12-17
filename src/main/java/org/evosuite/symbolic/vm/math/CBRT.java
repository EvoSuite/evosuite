package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class CBRT extends SymbolicFunction {

	private static final String CBRT = "cbrt";

	public CBRT(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, CBRT, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue cbrtExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.CBRT;
			cbrtExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			cbrtExpr = this.getSymbRealRetVal();
		}
		return cbrtExpr;
	}
}
