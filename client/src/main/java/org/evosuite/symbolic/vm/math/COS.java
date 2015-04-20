package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class COS extends SymbolicFunction {

	private static final String COS = "cos";

	public COS(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, COS, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue cosExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.COS;
			cosExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			cosExpr = this.getSymbRealRetVal();
		}
		return cosExpr;
	}
}
