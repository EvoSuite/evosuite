package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

public abstract class ULP extends MathFunction {

	private ULP(String owner, String name, String desc) {
		super(name, desc);
	}

	public static class ULP_D extends ULP {

		public ULP_D() {
			super("java.lang.Math", "ulp", MathFunctionCallVM.D2D_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, double res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.ULP;
				return new RealUnaryExpression(realExpression, op, res);
			} else
				return null;

		}

	}

	public static class ULP_F extends ULP {

		public ULP_F() {
			super("java.lang.Math", "ulp", MathFunctionCallVM.F2F_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, float res) {
			RealExpression realExpression = (RealExpression) params.pop();
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.ULP;
				return new RealUnaryExpression(realExpression, op, (double) res);
			} else
				return null;

		}

	}

}
