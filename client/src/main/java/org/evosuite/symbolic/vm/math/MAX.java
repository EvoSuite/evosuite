package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.bv.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class MAX {

	private static final String MAX = "max";

	public final static class MAX_D extends SymbolicFunction {

		public MAX_D(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, MAX, Types.DD2D_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			double res = this.getConcDoubleRetVal();
			RealValue left = this.getSymbRealArgument(0);
			RealValue right = this.getSymbRealArgument(1);
			RealValue maxExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.MAX;
				maxExpr = new RealBinaryExpression(left, op, right, res);
			} else {
				maxExpr = this.getSymbRealRetVal();
			}
			return maxExpr;
		}
	}

	public final static class MAX_F extends SymbolicFunction {

		public MAX_F(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, MAX, Types.FF2F_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			float res = this.getConcFloatRetVal();
			RealValue left = this.getSymbRealArgument(0);
			RealValue right = this.getSymbRealArgument(1);
			RealValue maxExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.MAX;
				maxExpr = new RealBinaryExpression(left, op, right,
						(double) res);
			} else {
				maxExpr = this.getSymbRealRetVal();
			}
			return maxExpr;
		}

	}

	public final static class MAX_I extends SymbolicFunction {

		public MAX_I(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, MAX, Types.II2I_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			int res = this.getConcIntRetVal();
			IntegerValue left = this.getSymbIntegerArgument(0);
			IntegerValue right = this.getSymbIntegerArgument(1);
			IntegerValue maxExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.MAX;
				maxExpr = new IntegerBinaryExpression(left, op, right,
						(long) res);
			} else {
				maxExpr = this.getSymbIntegerRetVal();
			}
			return maxExpr;
		}

	}

	public static class MAX_L extends SymbolicFunction {

		public MAX_L(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, MAX, Types.LL2L_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			long res = this.getConcLongRetVal();
			IntegerValue left = this.getSymbIntegerArgument(0);
			IntegerValue right = this.getSymbIntegerArgument(1);
			IntegerValue maxExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.MAX;
				maxExpr = new IntegerBinaryExpression(left, op, right, res);
			} else {
				maxExpr = this.getSymbIntegerRetVal();
			}
			return maxExpr;
		}

	}

}
