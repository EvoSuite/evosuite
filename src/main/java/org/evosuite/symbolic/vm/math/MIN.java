package org.evosuite.symbolic.vm.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class MIN {

	private static final String MIN = "min";

	public final static class MIN_D extends MathFunction_DD2D {

		public MIN_D(SymbolicEnvironment env) {
			super(env, MIN);
		}

		@Override
		protected RealExpression executeFunction(double res) {
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
		protected RealExpression executeFunction(float res) {
			RealBinaryExpression sym_val = new RealBinaryExpression(left,
					Operator.MIN, right, (double) res);
			return sym_val;
		}

	}

	public final static class MIN_I extends MathFunction_II2I {

		public MIN_I(SymbolicEnvironment env) {
			super(env, MIN);
		}

		public IntegerExpression execute(Stack<Expression<?>> params, int res) {
			IntegerExpression right = (IntegerExpression) params.pop();
			IntegerExpression left = (IntegerExpression) params.pop();
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				IntegerBinaryExpression sym_val = new IntegerBinaryExpression(
						left, Operator.MIN, right, (long) res);
				return sym_val;
			} else
				return null;
		}

		@Override
		protected IntegerExpression executeFunction(int res) {
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
		protected IntegerExpression executeFunction(long res) {
			IntegerBinaryExpression sym_val = new IntegerBinaryExpression(left,
					Operator.MIN, right, res);
			return sym_val;
		}

	}

}
