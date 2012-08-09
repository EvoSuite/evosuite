package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;

public abstract class MIN extends MathFunction {

	private MIN(String owner, String name, String desc) {
		super(name, desc);
	}

	public static class MIN_D extends MIN {

		public MIN_D() {
			super("java.lang.Math", "min", MathFunction.DD2D_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, double res) {
			RealExpression right = (RealExpression) params.pop();
			RealExpression left = (RealExpression) params.pop();
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				RealBinaryExpression sym_val = new RealBinaryExpression(left,
						Operator.MIN, right, res);
				return sym_val;
			} else
				return null;
		}

	}

	public static class MIN_F extends MIN {

		public MIN_F() {
			super("java.lang.Math", "min", MathFunction.FF2F_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, float res) {
			RealExpression right = (RealExpression) params.pop();
			RealExpression left = (RealExpression) params.pop();
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				RealBinaryExpression sym_val = new RealBinaryExpression(left,
						Operator.MIN, right, (double) res);
				return sym_val;
			} else
				return null;
		}

	}

	public static class MIN_I extends MIN {

		public MIN_I() {
			super("java.lang.Math", "min", MathFunction.II2I_DESCRIPTOR);
		}

		public IntegerExpression execute(Stack<Expression<?>> params, int res) {
			IntegerExpression right = (IntegerExpression) params.pop();
			IntegerExpression left = (IntegerExpression) params.pop();
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				IntegerBinaryExpression sym_val = new IntegerBinaryExpression(left,
						Operator.MIN, right, (long) res);
				return sym_val;
			} else
				return null;
		}

	}

	public static class MIN_L extends MIN {

		public MIN_L() {
			super("java.lang.Math", "abs", MathFunction.LL2L_DESCRIPTOR);
		}

		public IntegerExpression execute(Stack<Expression<?>> params, long res) {
			IntegerExpression right = (IntegerExpression) params.pop();
			IntegerExpression left = (IntegerExpression) params.pop();
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				IntegerBinaryExpression sym_val = new IntegerBinaryExpression(left,
						Operator.MIN, right, res);
				return sym_val;
			} else
				return null;
		}

	}

}
