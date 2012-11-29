package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class TANH extends SymbolicFunction {

	private static final String TANH = "tanh";

	public TANH(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, TANH, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue tanhExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.TANH;
			tanhExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			tanhExpr = this.getSymbRealRetVal();
		}
		return tanhExpr;
	}

}
