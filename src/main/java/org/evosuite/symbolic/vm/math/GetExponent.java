package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.RealUnaryToIntegerExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class GetExponent {

	private static final String GET_EXPONENT = "getExponent";

	public static class GetExponent_F extends MathFunction_F2I {

		public GetExponent_F(SymbolicEnvironment env) {
			super(env, GET_EXPONENT);
		}

		@Override
		protected IntegerValue executeFunction(int res) {
			Operator op = Operator.GETEXPONENT;
			RealUnaryToIntegerExpression realUnaryExpression = new RealUnaryToIntegerExpression(
					realExpression, op, (long) res);
			return realUnaryExpression;
		}

	}

	public static class GetExponent_D extends MathFunction_D2I {

		public GetExponent_D(SymbolicEnvironment env) {
			super(env, GET_EXPONENT);
		}

		@Override
		protected IntegerValue executeFunction(int res) {
			Operator op = Operator.GETEXPONENT;
			RealUnaryToIntegerExpression realUnaryExpression = new RealUnaryToIntegerExpression(
					realExpression, op, (long) res);
			return realUnaryExpression;
		}

	}

}
