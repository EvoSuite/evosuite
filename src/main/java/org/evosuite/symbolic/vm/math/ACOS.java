package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class ACOS extends SymbolicFunction {

	private static final String ACOS = "acos";

	public ACOS(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, ACOS, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue acosExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.ACOS;
			acosExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			acosExpr = this.getSymbRealRetVal();
		}
		return acosExpr;
	}

}
