package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealToIntegerCast;
import org.evosuite.symbolic.expr.RealUnaryExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class Round  {

	private static final String ROUND = "round";

	public static class Round_D extends MathFunction_D2L {

		public Round_D(SymbolicEnvironment env) {
			super(env, ROUND);
		}

		@Override
		protected IntegerExpression executeFunction(long res) {
			Operator op = Operator.ROUND;
			RealUnaryExpression realUnaryExpression = new RealUnaryExpression(
					realExpression, op, (double) res);
			return new RealToIntegerCast(realUnaryExpression, res);
		}

	}

	public static class Round_F extends MathFunction_F2I {

		public Round_F(SymbolicEnvironment env) {
			super(env, ROUND);
		}

		@Override
		protected IntegerExpression executeFunction(int res) {
			Operator op = Operator.ROUND;
			RealUnaryExpression realUnaryExpression = new RealUnaryExpression(
					realExpression, op, (double) res);
			return new RealToIntegerCast(realUnaryExpression, (long) res);
		}

	}

}
