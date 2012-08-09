package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

public abstract class NextUp extends MathFunction {

	private NextUp(String owner, String name, String desc) {
		super(name, desc);
	}

	public static class NextUp_D extends NextUp {

		public NextUp_D() {
			super("java.lang.Math", "nextUp",
					MathFunctionCallVM.D2D_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, double res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.NEXTUP;
				return new RealUnaryExpression(realExpression, op, res);
			} else
				return null;

		}

	}

	public static class NextUp_F extends NextUp {

		public NextUp_F() {
			super("java.lang.Math", "nextUp",
					MathFunctionCallVM.F2F_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, float res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.NEXTUP;
				return new RealUnaryExpression(realExpression, op, (double) res);
			} else
				return null;

		}

	}

}
