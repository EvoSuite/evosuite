package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealUnaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class SIGNUM {

	private static final String SIGNUM = "signum";

	public final static class SIGNUM_D extends SymbolicFunction {

		public SIGNUM_D(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, SIGNUM, Types.D2D_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			double res = this.getConcDoubleRetVal();
			RealValue realExpression = this.getSymbRealArgument(0);
			RealValue signumExpr;
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.SIGNUM;
				signumExpr = new RealUnaryExpression(realExpression, op, res);
			} else {
				signumExpr = this.getSymbRealRetVal();
			}
			return signumExpr;
		}

	}

	public final static class SIGNUM_F extends SymbolicFunction {

		public SIGNUM_F(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, SIGNUM, Types.F2F_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			float res = this.getConcFloatRetVal();
			RealValue realExpression = this.getSymbRealArgument(0);
			RealValue signumExpr;
			if (realExpression.containsSymbolicVariable()) {
				Operator op = Operator.SIGNUM;
				signumExpr = new RealUnaryExpression(realExpression, op,
						(double) res);
			} else {
				signumExpr = this.getSymbRealRetVal();
			}
			return signumExpr;
		}

	}

}
