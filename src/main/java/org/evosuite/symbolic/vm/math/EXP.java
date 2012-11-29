package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class EXP extends SymbolicFunction {

	private static final String EXP = "exp";

	public EXP(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, EXP, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue expExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.EXP;
			expExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			expExpr = this.getSymbRealRetVal();
		}
		return expExpr;
	}
}
