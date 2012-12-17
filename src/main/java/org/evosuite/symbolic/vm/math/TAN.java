package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class TAN extends SymbolicFunction {

	private static final String TAN = "tan";

	public TAN(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, TAN, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue tanExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.TAN;
			tanExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			tanExpr = this.getSymbRealRetVal();
		}
		return tanExpr;
	}

}
