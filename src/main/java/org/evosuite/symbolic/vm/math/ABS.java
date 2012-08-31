package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.bv.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class ABS {

	private static final String ABS_FUNCTION_NAME = "abs";

	public final static class ABS_D extends MathFunction_D2D {

		public ABS_D(SymbolicEnvironment env) {
			super(env, ABS_FUNCTION_NAME);
		}

		@Override
		protected RealValue executeFunction(double res) {
			RealUnaryExpression sym_val = new RealUnaryExpression(
					realExpression, Operator.ABS, res);
			return sym_val;
		}

	}

	public final static class ABS_F extends MathFunction_F2F {

		public ABS_F(SymbolicEnvironment env) {
			super(env, ABS_FUNCTION_NAME);
		}

		@Override
		protected RealValue executeFunction(float res) {
			RealUnaryExpression sym_val = new RealUnaryExpression(
					realExpression, Operator.ABS, (double) res);
			return sym_val;
		}

	}

	public final static class ABS_I extends MathFunction_I2I {

		public ABS_I(SymbolicEnvironment env) {
			super(env, ABS_FUNCTION_NAME);
		}

		@Override
		protected IntegerValue executeFunction(int res) {
			IntegerUnaryExpression sym_val = new IntegerUnaryExpression(
					integerExpression, Operator.ABS, (long) res);
			return sym_val;
		}

	}

	public final static class ABS_L extends MathFunction_L2L {

		public ABS_L(SymbolicEnvironment env) {
			super(env, ABS_FUNCTION_NAME);
		}

		@Override
		protected IntegerValue executeFunction(long res) {
			IntegerUnaryExpression sym_val = new IntegerUnaryExpression(
					integerExpression, Operator.ABS, res);
			return sym_val;
		}

	}

}
