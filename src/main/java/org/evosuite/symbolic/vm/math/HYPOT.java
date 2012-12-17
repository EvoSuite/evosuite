package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class HYPOT extends SymbolicFunction {

	private static final String HYPOT = "hypot";

	public HYPOT(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, HYPOT, Types.DD2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue left = this.getSymbRealArgument(0);
		RealValue right = this.getSymbRealArgument(1);
		RealValue hypotExpr;
		if (left.containsSymbolicVariable() || right.containsSymbolicVariable()) {
			Operator op = Operator.HYPOT;
			hypotExpr = new RealBinaryExpression(left, op, right, res);
		} else {
			hypotExpr = this.getSymbRealRetVal();
		}
		return hypotExpr;
	}

}
