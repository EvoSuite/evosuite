package edu.uta.cse.dsc.vm2.math;

import java.util.Stack;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.expr.RealUnaryExpression;

public abstract class SIGNUM extends MathFunction {

	private SIGNUM(String owner, String name, String desc) {
		super(name, desc);
	}

	public static class SIGNUM_D extends SIGNUM {

		public SIGNUM_D() {
			super("java.lang.Math", "signum",
					MathFunction.D2D_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, double res) {
			RealExpression param = (RealExpression) params.pop();
			if (param.containsSymbolicVariable()) {
				RealUnaryExpression sym_val = new RealUnaryExpression(param,
						Operator.SIGNUM, res);
				return sym_val;
			} else
				return null;
		}

	}

	public static class SIGNUM_F extends SIGNUM {

		public SIGNUM_F() {
			super("java.lang.Math", "signum",
					MathFunction.F2F_DESCRIPTOR);
		}

		public RealExpression execute(Stack<Expression<?>> params, float res) {
			RealExpression param = (RealExpression) params.pop();
			if (param.containsSymbolicVariable()) {
				RealUnaryExpression sym_val = new RealUnaryExpression(param,
						Operator.SIGNUM, (double) res);
				return sym_val;
			} else
				return null;
		}

	}

}
