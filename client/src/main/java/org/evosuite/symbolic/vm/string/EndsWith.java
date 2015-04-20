package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class EndsWith extends SymbolicFunction {

	private static final String ENDS_WITH = "endsWith";

	public EndsWith(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING, ENDS_WITH,
				Types.STR_TO_BOOL_DESCRIPTOR);
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

		boolean res = this.getConcBooleanRetVal();

		if (left_expr.containsSymbolicVariable()
				|| right_expr.containsSymbolicVariable()) {
			int conV = res ? 1 : 0;
			StringBinaryComparison strBExpr = new StringBinaryComparison(left_expr,
					Operator.ENDSWITH, right_expr, (long) conV);
			return strBExpr;
		} else {
			return this.getSymbIntegerRetVal();
		}
	}
}
