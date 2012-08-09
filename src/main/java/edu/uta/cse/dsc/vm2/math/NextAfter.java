package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;

public abstract class NextAfter extends MathFunction {

	private NextAfter(String owner, String name, String desc) {
		super(name, desc);
	}

	public static class NextAfter_D extends NextAfter {

		public NextAfter_D() {
			super("java.lang.Math", "nextAfter",
					MathFunctionCallVM.DD2D_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, double res) {
			RealExpression right = (RealExpression) params.pop();
			RealExpression left = (RealExpression) params.pop();
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.NEXTAFTER;
				return new RealBinaryExpression(left, op, right, res);
			} else {
				return null;
			}

		}

	}

	public static class NextAfter_F extends NextAfter {

		public NextAfter_F() {
			super("java.lang.Math", "nextAfter",
					MathFunctionCallVM.FD2F_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, float res) {
			RealExpression right = (RealExpression) params.pop();
			RealExpression left = (RealExpression) params.pop();
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.NEXTAFTER;
				return new RealBinaryExpression(left, op, right, (double) res);
			} else {
				return null;
			}

		}

	}

}
