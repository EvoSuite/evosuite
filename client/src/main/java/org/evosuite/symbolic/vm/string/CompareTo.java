package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringBinaryToIntegerExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class CompareTo extends SymbolicFunction {

	private static final String COMPARE_TO = "compareTo";

	public CompareTo(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING, COMPARE_TO,
				Types.STR_TO_INT_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		String conc_left = (String) this.getConcReceiver();
		NonNullReference symb_left = this.getSymbReceiver();

		StringValue left_expr = env.heap.getField(Types.JAVA_LANG_STRING,
				SymbolicHeap.$STRING_VALUE, conc_left, symb_left, conc_left);

		String conc_right = (String) this.getConcArgument(0);
		NonNullReference symb_right = (NonNullReference) this
				.getSymbArgument(0);

		StringValue right_expr = env.heap.getField(Types.JAVA_LANG_STRING,
				SymbolicHeap.$STRING_VALUE, conc_right, symb_right, conc_right);

		int res = this.getConcIntRetVal();

		if (left_expr.containsSymbolicVariable()
				|| right_expr.containsSymbolicVariable()) {
			StringBinaryToIntegerExpression strBExpr = new StringBinaryToIntegerExpression(
					left_expr, Operator.COMPARETO, right_expr, (long) res);

			return strBExpr;
		} else {

			return this.getSymbIntegerRetVal();
		}
	}
}
