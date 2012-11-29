package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class RINT extends SymbolicFunction {

	private static final String RINT = "rint";

	public RINT(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, RINT, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue rintExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.RINT;
			rintExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			rintExpr = this.getSymbRealRetVal();
		}
		return rintExpr;
	}
}
