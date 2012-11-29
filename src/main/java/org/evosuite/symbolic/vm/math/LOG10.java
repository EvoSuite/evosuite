package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public final class LOG10 extends SymbolicFunction {

	private static final String LOG10 = "log10";

	public LOG10(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, LOG10, Types.D2D_DESCRIPTOR);
	}
	
	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue log10Expr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.LOG10;
			log10Expr = new RealUnaryExpression(realExpression, op, res);
		} else {
			log10Expr = this.getSymbRealRetVal();
		}
		return log10Expr;
	}


}
