package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

public abstract class Round extends MathFunction {

	private Round(String owner, String name, String desc) {
		super(name, desc);
	}

	public static class Round_D extends Round {

		public Round_D() {
			super("java.lang.Math", "round", MathFunction.D2D_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, double res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.ROUND;
				return new RealUnaryExpression(realExpression, op, res);
			} else
				return null;

		}

	}

	public static class Round_F extends Round {

		public Round_F() {
			super("java.lang.Math", "round", MathFunction.F2F_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, float res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.ROUND;
				return new RealUnaryExpression(realExpression, op, (double) res);
			} else
				return null;

		}

	}

}
