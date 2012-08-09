package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.IntegerUnaryExpression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

public abstract class ABS extends MathFunction {

	private ABS(String owner, String name, String desc) {
		super(name, desc);
	}

	public static class ABS_D extends ABS {

		public ABS_D() {
			super("java.lang.Math", "abs", MathFunctionCallVM.D2D_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, double res) {
			RealExpression param = (RealExpression) params.pop();
			if (param.containsSymbolicVariable()) {
				RealUnaryExpression sym_val = new RealUnaryExpression(param,
						Operator.ABS, res);
				return sym_val;
			} else
				return null;
		}

	}

	public static class ABS_F extends ABS {

		public ABS_F() {
			super("java.lang.Math", "abs", MathFunctionCallVM.F2F_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, float res) {
			RealExpression param = (RealExpression) params.pop();
			if (param.containsSymbolicVariable()) {
				RealUnaryExpression sym_val = new RealUnaryExpression(param,
						Operator.ABS, (double) res);
				return sym_val;
			} else
				return null;
		}

	}

	public static class ABS_I extends ABS {

		public ABS_I() {
			super("java.lang.Math", "abs", MathFunctionCallVM.I2I_DESCRIPTOR);
		}

		public IntegerExpression execute(Stack<Expression<?>> params, int res) {
			IntegerExpression param = (IntegerExpression) params.pop();
			if (param.containsSymbolicVariable()) {
				IntegerUnaryExpression sym_val = new IntegerUnaryExpression(
						param, Operator.ABS, (long) res);
				return sym_val;
			} else
				return null;
		}

	}

	public static class ABS_L extends ABS {

		public ABS_L() {
			super("java.lang.Math", "abs", MathFunctionCallVM.L2L_DESCRIPTOR);
		}

		public IntegerExpression execute(Stack<Expression<?>> params, long res) {
			IntegerExpression param = (IntegerExpression) params.pop();
			if (param.containsSymbolicVariable()) {
				IntegerUnaryExpression sym_val = new IntegerUnaryExpression(
						param, Operator.ABS, res);
				return sym_val;
			} else
				return null;
		}

	}

}
