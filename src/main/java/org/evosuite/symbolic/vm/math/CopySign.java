package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class CopySign {

	private static final String COPY_SIGN = "copySign";

	public static final class CopySign_F extends SymbolicFunction {

		public CopySign_F(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, COPY_SIGN, Types.FF2F_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			float res = this.getConcFloatRetVal();
			RealValue left = this.getSymbRealArgument(0);
			RealValue right = this.getSymbRealArgument(1);
			RealValue copySignExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.COPYSIGN;
				copySignExpr = new RealBinaryExpression(left, op, right,
						(double) res);
			} else {
				copySignExpr = this.getSymbRealRetVal();
			}
			return copySignExpr;
		}

	}

	public final static class CopySign_D extends SymbolicFunction {

		private static final String COPY_SIGN = "copySign";

		public CopySign_D(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, COPY_SIGN, Types.DD2D_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			double res = this.getConcDoubleRetVal();
			RealValue left = this.getSymbRealArgument(0);
			RealValue right = this.getSymbRealArgument(1);
			RealValue copySignExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.COPYSIGN;
				copySignExpr = new RealBinaryExpression(left, op, right, res);
			} else {
				copySignExpr = this.getSymbRealRetVal();
			}
			return copySignExpr;
		}

	}

}
