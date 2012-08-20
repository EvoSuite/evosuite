package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class NextAfter  {

	private static final String NEXT_AFTER = "nextAfter";

	public final static class NextAfter_D extends MathFunction_DD2D {

		public NextAfter_D(SymbolicEnvironment env) {
			super(env, NEXT_AFTER);
		}

		@Override
		protected RealExpression executeFunction(double res) {
			Operator op = Operator.NEXTAFTER;
			return new RealBinaryExpression(left, op, right, res);
		}

	}

	public static class NextAfter_F extends MathFunction_FD2F {

		public NextAfter_F(SymbolicEnvironment env) {
			super(env, NEXT_AFTER);
		}

		@Override
		protected RealExpression executeFunction(float res) {
			Operator op = Operator.NEXTAFTER;
			return new RealBinaryExpression(left, op, right, (double) res);
		}
	}

}
