package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.RealUnaryToIntegerExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class Round {

	private static final String ROUND = "round";

	public static class Round_D extends MathFunction_D2L {

		public Round_D(SymbolicEnvironment env) {
			super(env, ROUND);
		}

		@Override
		protected IntegerValue executeFunction(long res) {
			Operator op = Operator.ROUND;
			RealUnaryToIntegerExpression realUnaryExpression = new RealUnaryToIntegerExpression(
					realExpression, op, res);
			return realUnaryExpression;
		}

	}

	public static class Round_F extends MathFunction_F2I {

		public Round_F(SymbolicEnvironment env) {
			super(env, ROUND);
		}

		@Override
		protected IntegerValue executeFunction(int res) {
			Operator op = Operator.ROUND;
			RealUnaryToIntegerExpression realUnaryExpression = new RealUnaryToIntegerExpression(
					realExpression, op, (long) res);
			return realUnaryExpression;
		}

	}

}
