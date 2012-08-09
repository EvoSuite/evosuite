package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;

public abstract class SCALB extends MathFunction {

	private SCALB(String owner, String name, String desc) {
		super(name, desc);
	}

	public static class SCALB_D extends SCALB {

		public SCALB_D() {
			super("java.lang.Math", "scalb",
					MathFunctionCallVM.DI2D_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, double res) {
			IntegerExpression right = (IntegerExpression) params.pop();
			RealExpression left = (RealExpression) params.pop();
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				RealBinaryExpression sym_val = new RealBinaryExpression(left,
						Operator.SCALB, right, res);
				return sym_val;
			} else
				return null;
		}

	}

	public static class SCALB_F extends SCALB {

		public SCALB_F() {
			super("java.lang.Math", "scalb",
					MathFunctionCallVM.FI2F_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, float res) {
			IntegerExpression right = (IntegerExpression) params.pop();
			RealExpression left = (RealExpression) params.pop();
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				RealBinaryExpression sym_val = new RealBinaryExpression(left,
						Operator.SCALB, right, (double) res);
				return sym_val;
			} else
				return null;
		}

	}

}
