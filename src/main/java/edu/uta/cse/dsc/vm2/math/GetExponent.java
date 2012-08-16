package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealToIntegerCast;
import org.evosuite.symbolic.expr.RealUnaryExpression;

public abstract class GetExponent extends MathFunction {

	public static class GetExponent_F extends GetExponent {

		public GetExponent_F() {
			super("java.lang.Math", "getExponent", MathFunction.F2I_DESCRIPTOR);
		}

		public IntegerExpression execute(Stack<Expression<?>> params, int res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.GETEXPONENT;
				RealUnaryExpression realUnaryExpression = new RealUnaryExpression(
						realExpression, op, (double) res);
				return new RealToIntegerCast(realUnaryExpression, (long) res);
			} else
				return null;

		}

	}

	private GetExponent(String owner, String name, String desc) {
		super(name, desc);
	}

	public static class GetExponent_D extends GetExponent {

		public GetExponent_D() {
			super("java.lang.Math", "getExponent", MathFunction.D2I_DESCRIPTOR);
		}

		public IntegerExpression execute(Stack<Expression<?>> params, int res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.GETEXPONENT;
				RealUnaryExpression realUnaryExpression = new RealUnaryExpression(
						realExpression, op, (double) res);
				return new RealToIntegerCast(realUnaryExpression, (long) res);
			} else
				return null;

		}

	}

}
