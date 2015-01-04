package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class NextUp {

	private static final String NEXT_UP = "nextUp";

	public final static class NextUp_D extends SymbolicFunction {

		public NextUp_D(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, NEXT_UP, Types.D2D_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			double res = this.getConcDoubleRetVal();
			RealValue realExpression = this.getSymbRealArgument(0);
			RealValue nextUpExpr;
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.NEXTUP;
				nextUpExpr = new RealUnaryExpression(realExpression, op, res);
			} else {
				nextUpExpr = this.getSymbRealRetVal();
			}
			return nextUpExpr;
		}

	}

	public final static class NextUp_F extends SymbolicFunction {

		public NextUp_F(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, NEXT_UP, Types.F2F_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			float res = this.getConcFloatRetVal();
			RealValue realExpression = this.getSymbRealArgument(0);
			RealValue nextUpExpr;
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.NEXTUP;
				nextUpExpr = new RealUnaryExpression(realExpression, op,
						(double) res);
			} else {
				nextUpExpr = this.getSymbRealRetVal();
			}
			return nextUpExpr;
		}

	}

}
