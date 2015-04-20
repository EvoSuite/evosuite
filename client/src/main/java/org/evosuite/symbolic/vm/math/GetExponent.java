package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.RealUnaryToIntegerExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class GetExponent {

	private static final String GET_EXPONENT = "getExponent";

	public static class GetExponent_F extends SymbolicFunction {

		public GetExponent_F(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, GET_EXPONENT, Types.F2I_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {

			int res = this.getConcIntRetVal();
			RealValue realExpression = this.getSymbRealArgument(0);

			IntegerValue getExponentExpr;
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.GETEXPONENT;
				getExponentExpr = new RealUnaryToIntegerExpression(
						realExpression, op, (long) res);
			} else {
				getExponentExpr = this.getSymbIntegerRetVal();
			}
			return getExponentExpr;
		}

	}

	public static class GetExponent_D extends SymbolicFunction {

		public GetExponent_D(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, GET_EXPONENT, Types.D2I_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {

			int res = this.getConcIntRetVal();
			RealValue realExpression = this.getSymbRealArgument(0);

			IntegerValue getExponentExpr;
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.GETEXPONENT;
				getExponentExpr = new RealUnaryToIntegerExpression(
						realExpression, op, (long) res);
			} else {
				getExponentExpr = this.getSymbIntegerRetVal();
			}
			return getExponentExpr;
		}

	}

}
