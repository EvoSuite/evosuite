package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class SIGNUM  {

	private static final String SIGNUM = "signum";

	public final static class SIGNUM_D extends MathFunction_D2D {

		public SIGNUM_D(SymbolicEnvironment env) {
			super(env, SIGNUM);
		}

		@Override
		protected RealValue executeFunction(double res) {
			RealUnaryExpression sym_val = new RealUnaryExpression(
					realExpression, Operator.SIGNUM, res);
			return sym_val;
		}

	}

	public final static class SIGNUM_F extends MathFunction_F2F {

		public SIGNUM_F(SymbolicEnvironment env) {
			super(env, SIGNUM);
		}

		@Override
		protected RealValue executeFunction(float res) {
			RealUnaryExpression sym_val = new RealUnaryExpression(
					realExpression, Operator.SIGNUM, (double) res);
			return sym_val;
		}

	}

}
