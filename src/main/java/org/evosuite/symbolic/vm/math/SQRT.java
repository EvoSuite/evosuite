package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class SQRT extends SymbolicFunction {

	private static final String SQRT = "sqrt";

	public SQRT(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, SQRT, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue sqrtExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.SQRT;
			sqrtExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			sqrtExpr = this.getSymbRealRetVal();
		}
		return sqrtExpr;
	}

}
