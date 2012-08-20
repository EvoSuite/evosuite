package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class NextUp  {

	private static final String NEXT_UP = "nextUp";

	public final static class NextUp_D extends MathFunction_D2D {

		public NextUp_D(SymbolicEnvironment env) {
			super(env, NEXT_UP);
		}

		@Override
		protected RealExpression executeFunction(double res) {
			Operator op = Operator.NEXTUP;
			return new RealUnaryExpression(realExpression, op, res);
		}

	}

	public final static class NextUp_F extends MathFunction_F2F {

		public NextUp_F(SymbolicEnvironment env) {
			super(env, NEXT_UP);
		}

		@Override
		protected RealExpression executeFunction(float res) {
			Operator op = Operator.NEXTUP;
			return new RealUnaryExpression(realExpression, op, (double) res);
		}

	}

}
