package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class ULP {

	private static final String ULP = "ulp";

	public final static class ULP_D extends SymbolicFunction {

		public ULP_D(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, ULP, Types.D2D_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			double res = this.getConcDoubleRetVal();
			RealValue realExpression = this.getSymbRealArgument(0);
			RealValue ulpExpr;
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.ULP;
				ulpExpr = new RealUnaryExpression(realExpression, op, res);
			} else {
				ulpExpr = this.getSymbRealRetVal();
			}
			return ulpExpr;
		}

	}

	public final static class ULP_F extends SymbolicFunction {

		public ULP_F(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, ULP, Types.F2F_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			float res = this.getConcFloatRetVal();
			RealValue realExpression = this.getSymbRealArgument(0);
			RealValue ulpExpr;
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.ULP;
				ulpExpr = new RealUnaryExpression(realExpression, op,
						(double) res);
			} else {
				ulpExpr = this.getSymbRealRetVal();
			}
			return ulpExpr;
		}

	}

}
