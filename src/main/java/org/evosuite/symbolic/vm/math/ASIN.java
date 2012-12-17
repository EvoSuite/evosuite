package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class ASIN extends SymbolicFunction {

	private static final String ASIN = "asin";

	public ASIN(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, ASIN, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue asinExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.ASIN;
			asinExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			asinExpr = this.getSymbRealRetVal();
		}
		return asinExpr;
	}

}
