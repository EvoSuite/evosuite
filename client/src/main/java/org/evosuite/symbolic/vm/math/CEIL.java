package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class CEIL extends SymbolicFunction {

	private static final String CEIL = "ceil";

	public CEIL(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, CEIL, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue ceilExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.CEIL;
			ceilExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			ceilExpr = this.getSymbRealRetVal();
		}
		return ceilExpr;
	}
}
