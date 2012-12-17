package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class COSH extends SymbolicFunction {

	private static final String COSH = "cosh";

	public COSH(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, COSH, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue coshExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.COSH;
			coshExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			coshExpr = this.getSymbRealRetVal();
		}
		return coshExpr;
	}
}
