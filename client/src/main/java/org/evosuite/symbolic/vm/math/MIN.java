package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerBinaryExpression;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealBinaryExpression;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;

public abstract class MIN {

	private static final String MIN = "min";

	public final static class MIN_D extends SymbolicFunction {

		public MIN_D(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, MIN, Types.DD2D_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			double res = this.getConcDoubleRetVal();
			RealValue left = this.getSymbRealArgument(0);
			RealValue right = this.getSymbRealArgument(1);
			RealValue minExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.MIN;
				minExpr = new RealBinaryExpression(left, op, right, res);
			} else {
				minExpr = this.getSymbRealRetVal();
			}
			return minExpr;
		}

	}

	public final static class MIN_F extends SymbolicFunction {

		public MIN_F(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, MIN, Types.FF2F_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			float res = this.getConcFloatRetVal();
			RealValue left = this.getSymbRealArgument(0);
			RealValue right = this.getSymbRealArgument(1);
			RealValue minExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.MIN;
				minExpr = new RealBinaryExpression(left, op, right,
						(double) res);
			} else {
				minExpr = this.getSymbRealRetVal();
			}
			return minExpr;
		}

	}

	public final static class MIN_I extends SymbolicFunction {

		public MIN_I(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, MIN, Types.II2I_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			int res = this.getConcIntRetVal();
			IntegerValue left = this.getSymbIntegerArgument(0);
			IntegerValue right = this.getSymbIntegerArgument(1);
			IntegerValue minExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.MIN;
				minExpr = new IntegerBinaryExpression(left, op, right,
						(long) res);
			} else {
				minExpr = this.getSymbIntegerRetVal();
			}
			return minExpr;
		}

	}

	public static class MIN_L extends SymbolicFunction {

		public MIN_L(SymbolicEnvironment env) {
			super(env, Types.JAVA_LANG_MATH, MIN, Types.LL2L_DESCRIPTOR);
		}

		@Override
		public Object executeFunction() {
			long res = this.getConcLongRetVal();
			IntegerValue left = this.getSymbIntegerArgument(0);
			IntegerValue right = this.getSymbIntegerArgument(1);
			IntegerValue minExpr;
			if (left.containsSymbolicVariable()
					|| right.containsSymbolicVariable()) {
				Operator op = Operator.MIN;
				minExpr = new IntegerBinaryExpression(left, op, right, res);
			} else {
				minExpr = this.getSymbIntegerRetVal();
			}
			return minExpr;
		}

	}

}
