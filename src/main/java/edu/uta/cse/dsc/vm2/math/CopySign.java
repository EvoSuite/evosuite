package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;

public abstract class CopySign extends MathFunction {

	public static class CopySign_F extends CopySign {

		public CopySign_F() {
			super("java.lang.Math", "copySign",
					MathFunctionCallVM.FF2F_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, float res) {
			RealExpression right = (RealExpression) params.pop();
			RealExpression left = (RealExpression) params.pop();
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.COPYSIGN;
				return new RealBinaryExpression(left, op, right, (double) res);
			} else {
				return null;
			}

		}

	}

	public static class CopySign_D extends CopySign {

		public CopySign_D() {
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

	private CopySign(String owner, String name, String desc) {
		super(name, desc);
	}

}
