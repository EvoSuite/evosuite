package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class EXPM1 extends SymbolicFunction {

	private static final String EXPM1 = "expm1";

	public EXPM1(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, EXPM1, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue expm1Expr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.EXPM1;
			expm1Expr = new RealUnaryExpression(realExpression, op, res);
		} else {
			expm1Expr = this.getSymbRealRetVal();
		}
		return expm1Expr;
	}
}
