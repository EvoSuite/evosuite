package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class ToDegrees extends SymbolicFunction {

	private static final String TO_DEGREES = "toDegrees";

	public ToDegrees(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, TO_DEGREES, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue toDegreesExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.TODEGREES;
			toDegreesExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			toDegreesExpr = this.getSymbRealRetVal();
		}
		return toDegreesExpr;
	}

}
