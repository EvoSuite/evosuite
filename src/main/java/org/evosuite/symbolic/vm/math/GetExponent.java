package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealToIntegerCast;
import org.evosuite.symbolic.expr.RealUnaryExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class GetExponent  {

	private static final String GET_EXPONENT = "getExponent";

	public static class GetExponent_F extends MathFunction_F2I {

		public GetExponent_F(SymbolicEnvironment env) {
			super(env, GET_EXPONENT);
		}

		@Override
		protected IntegerExpression executeFunction(int res) {
			Operator op = Operator.GETEXPONENT;
			RealUnaryExpression realUnaryExpression = new RealUnaryExpression(
					realExpression, op, (double) res);
			return new RealToIntegerCast(realUnaryExpression, (long) res);
		}

	}

	public static class GetExponent_D extends MathFunction_D2I {

		public GetExponent_D(SymbolicEnvironment env) {
			super(env, GET_EXPONENT);
		}

		@Override
		protected IntegerExpression executeFunction(int res) {
			Operator op = Operator.GETEXPONENT;
			RealUnaryExpression realUnaryExpression = new RealUnaryExpression(
					realExpression, op, (double) res);
			return new RealToIntegerCast(realUnaryExpression, (long) res);
		}

	}

}
