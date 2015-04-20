package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class SCALB {

	public static class SCALB_D extends SymbolicFunction {

		public SCALB_D(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, SCALB, Types.DI2D_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			double res = this.getConcDoubleRetVal();
			RealValue left = this.getSymbRealArgument(0);
			IntegerValue right = this.getSymbIntegerArgument(1);
			RealValue scalbExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.SCALB;
				scalbExpr = new RealBinaryExpression(left, op, right, res);
			} else {
				scalbExpr = this.getSymbRealRetVal();
			}
			return scalbExpr;
		}

	}

	private static final String SCALB = "scalb";

	public static class SCALB_F extends SymbolicFunction {

		public SCALB_F(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, SCALB, Types.FI2F_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			float res = this.getConcFloatRetVal();
			RealValue left = this.getSymbRealArgument(0);
			IntegerValue right = this.getSymbIntegerArgument(1);
			RealValue scalbExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.SCALB;
				scalbExpr = new RealBinaryExpression(left, op, right,
						(double) res);
			} else {
				scalbExpr = this.getSymbRealRetVal();
			}
			return scalbExpr;
		}


	}

}
