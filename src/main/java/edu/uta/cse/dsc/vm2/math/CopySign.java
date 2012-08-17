package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class CopySign {

	private static final String COPY_SIGN = "copySign";

	public static final class CopySign_F extends MathFunction_FF2F {

		public CopySign_F(SymbolicEnvironment env) {
			super(env, COPY_SIGN);
		}

		@Override
		protected RealExpression executeFunction(float res) {
			Operator op = Operator.COPYSIGN;
			return new RealBinaryExpression(left, op, right, (double) res);
		}

	}

	public final static class CopySign_D extends MathFunction_DD2D {

		private static final String COPY_SIGN = "copySign";

		public CopySign_D(SymbolicEnvironment env) {
			super(env, COPY_SIGN);
		}

		@Override
		protected RealExpression executeFunction(double res) {
			Operator op = Operator.COPYSIGN;
			return new RealBinaryExpression(left, op, right, res);
		}

	}

}
