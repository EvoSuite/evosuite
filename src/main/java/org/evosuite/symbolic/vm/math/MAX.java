package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class MAX {

	private static final String MAX = "max";

	public final static class MAX_D extends MathFunction_DD2D {

		public MAX_D(SymbolicEnvironment env) {
			super(env, MAX);
		}

		@Override
		protected RealExpression executeFunction(double res) {
			RealBinaryExpression sym_val = new RealBinaryExpression(left,
					Operator.MAX, right, res);
			return sym_val;
		}

	}

	public final static class MAX_F extends MathFunction_FF2F {

		public MAX_F(SymbolicEnvironment env) {
			super(env, MAX);
		}

		@Override
		protected RealExpression executeFunction(float res) {
			RealBinaryExpression sym_val = new RealBinaryExpression(left,
					Operator.MAX, right, (double) res);
			return sym_val;
		}

	}

	public final static class MAX_I extends MathFunction_II2I {

		public MAX_I(SymbolicEnvironment env) {
			super(env, MAX);
		}

		@Override
		protected IntegerExpression executeFunction(int res) {
			IntegerBinaryExpression sym_val = new IntegerBinaryExpression(left,
					Operator.MAX, right, (long) res);
			return sym_val;
		}

	}

	public static class MAX_L extends MathFunction_LL2L {

		public MAX_L(SymbolicEnvironment env) {
			super(env, MAX);
		}

		@Override
		protected IntegerExpression executeFunction(long res) {
			IntegerBinaryExpression sym_val = new IntegerBinaryExpression(left,
					Operator.MAX, right, res);
			return sym_val;
		}

	}

}
