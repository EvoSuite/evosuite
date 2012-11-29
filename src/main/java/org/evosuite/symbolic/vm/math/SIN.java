package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class SIN extends SymbolicFunction {

	private static final String SIN = "sin";

	public SIN(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, SIN, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue sinExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.SIN;
			sinExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			sinExpr = this.getSymbRealRetVal();
		}
		return sinExpr;
	}

}
