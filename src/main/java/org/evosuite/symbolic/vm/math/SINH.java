package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class SINH extends SymbolicFunction {

	private static final String SINH = "sinh";

	public SINH(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, SINH, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue sinhExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.SINH;
			sinhExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			sinhExpr = this.getSymbRealRetVal();
		}
		return sinhExpr;
	}

}
