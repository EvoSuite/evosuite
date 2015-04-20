package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public final class FLOOR extends SymbolicFunction {

	private static final String FLOOR = "floor";

	public FLOOR(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_MATH, FLOOR, Types.D2D_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		double res = this.getConcDoubleRetVal();
		RealValue realExpression = this.getSymbRealArgument(0);
		RealValue floorExpr;
		if (realExpression.containsSymbolicVariable()) {
			Operator op = Operator.FLOOR;
			floorExpr = new RealUnaryExpression(realExpression, op, res);
		} else {
			floorExpr = this.getSymbRealRetVal();
		}
		return floorExpr;
	}
}
