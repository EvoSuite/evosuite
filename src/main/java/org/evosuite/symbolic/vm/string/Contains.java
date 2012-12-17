package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Contains extends SymbolicFunction {

	private static final String CONTAINS = "contains";

	public Contains(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING, CONTAINS,
				Types.CHARSEQ_TO_BOOL_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		String conc_left = (String) this.getConcReceiver();
		NonNullReference symb_left = this.getSymbReceiver();

		StringValue left_expr = env.heap.getField(Types.JAVA_LANG_STRING,
				SymbolicHeap.$STRING_VALUE, conc_left, symb_left, conc_left);

		CharSequence conc_right = (CharSequence) this.getConcArgument(0);
		NonNullReference symb_right = (NonNullReference) this
				.getSymbArgument(0);

		if (conc_right instanceof String) {
			String conc_right_str = (String) conc_right;
			StringValue right_expr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_right_str, symb_right,
					conc_right_str);

			boolean res = this.getConcBooleanRetVal();
			if (right_expr != null) {
				if (left_expr.containsSymbolicVariable()
						|| right_expr.containsSymbolicVariable()) {

					int concrete_value = res ? 1 : 0;

					StringBinaryComparison strComp = new StringBinaryComparison(left_expr,
							Operator.CONTAINS, right_expr,
							(long) concrete_value);

					return strComp;
				}
			}
		}

		return this.getSymbIntegerRetVal();
	}

}