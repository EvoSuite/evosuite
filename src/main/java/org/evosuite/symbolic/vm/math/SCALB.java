package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class SCALB {

	public static class SCALB_D extends MathFunction_DI2D {

		public SCALB_D(SymbolicEnvironment env) {
			super(env, SCALB);
		}

		@Override
		protected RealValue executeFunction(double res) {
			RealBinaryExpression sym_val = new RealBinaryExpression(left,
					Operator.SCALB, right, res);
			return sym_val;
		}

	}

	private static final String SCALB = "scalb";

	public static class SCALB_F extends MathFunction_FI2F {

		public SCALB_F(SymbolicEnvironment env) {
			super(env, SCALB);
		}

		@Override
		protected RealValue executeFunction(float res) {
			RealBinaryExpression sym_val = new RealBinaryExpression(left,
					Operator.SCALB, right, (double) res);
			return sym_val;
		}

	}

}
