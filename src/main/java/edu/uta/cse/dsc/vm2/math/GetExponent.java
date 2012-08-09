package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

public abstract class GetExponent extends MathFunction {

	public static class GetExponent_F extends GetExponent {

		public GetExponent_F() {
			super("java.lang.Math", "getExponent",
					MathFunctionCallVM.F2F_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, float res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.GETEXPONENT;
				return new RealUnaryExpression(realExpression, op, (double) res);
			} else
				return null;

		}

	}

	private GetExponent(String owner, String name, String desc) {
		super(name, desc);
	}

	public static class GetExponent_D extends GetExponent {

		public GetExponent_D() {
			super("java.lang.Math", "getExponent",
					MathFunctionCallVM.D2D_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, double res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.GETEXPONENT;
				return new RealUnaryExpression(realExpression, op, res);
			} else
				return null;

		}

	}

}
