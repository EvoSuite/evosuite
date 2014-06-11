package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class ATAN extends SymbolicFunction {

	private static final String ATAN = "atan";

	public ATAN(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, ATAN, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue atanExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.ATAN;
			atanExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			atanExpr = this.getSymbRealRetVal();
		}
		return atanExpr;
	}

}
