package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class ULP {

	private static final String FUNCTION_ULP = "ulp";

	public final static class ULP_D extends MathFunction_D2D {

		public ULP_D(SymbolicEnvironment env) {
			super(env, FUNCTION_ULP);
		}

		@Override
		protected RealExpression executeFunction(double res) {
			Operator op = Operator.ULP;
			return new RealUnaryExpression(realExpression, op, res);
		}

	}

	public final static class ULP_F extends MathFunction_F2F {

		public ULP_F(SymbolicEnvironment env) {
			super(env, FUNCTION_ULP);
		}

		@Override
		protected RealExpression executeFunction(float res) {
			Operator op = Operator.ULP;
			return new RealUnaryExpression(realExpression, op, (double) res);
		}

	}

}
