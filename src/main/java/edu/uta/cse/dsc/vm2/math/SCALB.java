package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.RealBinaryExpression;
import org.evosuite.symbolic.expr.RealExpression;

import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class SCALB {

	public static class SCALB_D extends MathFunction_DI2D {

		public SCALB_D(SymbolicEnvironment env) {
			super(env, SCALB);
		}

		@Override
		protected RealExpression executeFunction(double res) {
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
		protected RealExpression executeFunction(float res) {
			RealBinaryExpression sym_val = new RealBinaryExpression(left,
					Operator.SCALB, right, (double) res);
			return sym_val;
		}

	}

}
