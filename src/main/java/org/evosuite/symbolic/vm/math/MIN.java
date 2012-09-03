package org.evosuite.symbolic.vm.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.bv.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class MIN {

	private static final String MIN = "min";

	public final static class MIN_D extends MathFunction_DD2D {

		public MIN_D(SymbolicEnvironment env) {
			super(env, MIN);
		}

		@Override
		protected RealValue executeFunction(double res) {
			RealBinaryExpression sym_val = new RealBinaryExpression(left,
					Operator.MIN, right, res);
			return sym_val;
		}

	}

	public final static class MIN_F extends MathFunction_FF2F {

		public MIN_F(SymbolicEnvironment env) {
			super(env, MIN);
		}

		@Override
		protected RealValue executeFunction(float res) {
			RealBinaryExpression sym_val = new RealBinaryExpression(left,
					Operator.MIN, right, (double) res);
			return sym_val;
		}

	}

	public final static class MIN_I extends MathFunction_II2I {

		public MIN_I(SymbolicEnvironment env) {
			super(env, MIN);
		}

		public IntegerValue execute(Stack<Expression<?>> params, int res) {
			IntegerValue right = (IntegerValue) params.pop();
			IntegerValue left = (IntegerValue) params.pop();
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				IntegerBinaryExpression sym_val = new IntegerBinaryExpression(
						left, Operator.MIN, right, (long) res);
				return sym_val;
			} else
				return null;
		}

		@Override
		protected IntegerValue executeFunction(int res) {
			IntegerBinaryExpression sym_val = new IntegerBinaryExpression(left,
					Operator.MIN, right, (long) res);
			return sym_val;
		}

	}

	public static class MIN_L extends MathFunction_LL2L {

		public MIN_L(SymbolicEnvironment env) {
			super(env, MIN);
		}

		@Override
		protected IntegerValue executeFunction(long res) {
			IntegerBinaryExpression sym_val = new IntegerBinaryExpression(left,
					Operator.MIN, right, res);
			return sym_val;
		}

	}

}
