package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealToIntegerCast;
import org.evosuite.symbolic.expr.RealUnaryExpression;

public abstract class Round extends MathFunction {

	private Round(String owner, String name, String desc) {
		super(name, desc);
	}

	public static class Round_D extends Round {

		public Round_D() {
			super("java.lang.Math", "round", MathFunction.D2L_DESCRIPTOR);
		}

		public IntegerExpression execute(Stack<Expression<?>> params, long res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.ROUND;
				RealUnaryExpression realUnaryExpression = new RealUnaryExpression(
						realExpression, op, (double) res);
				return new RealToIntegerCast(realUnaryExpression, res);
			} else
				return null;

		}

	}

	public static class Round_F extends Round {

		public Round_F() {
			super("java.lang.Math", "round", MathFunction.F2I_DESCRIPTOR);
		}

		public IntegerExpression execute(Stack<Expression<?>> params, int res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.ROUND;
				RealUnaryExpression realUnaryExpression = new RealUnaryExpression(
						realExpression, op, (double) res);
				return new RealToIntegerCast(realUnaryExpression, (long) res);
			} else
				return null;

		}

	}

}
