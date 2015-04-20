package org.evosuite.symbolic.vm.string;

import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.StringBinaryComparison;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Equals extends SymbolicFunction {

	private static final String EQUALS = "equals";

	public Equals(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING, EQUALS,
				Types.OBJECT_TO_BOOL_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		String conc_left = (String) this.getConcReceiver();
		NonNullReference symb_left = this.getSymbReceiver();

		Object conc_right = this.getConcArgument(0);
		Reference symb_right = this.getSymbArgument(0);

		boolean res = this.getConcBooleanRetVal();

		StringValue left_expr = env.heap.getField(Types.JAVA_LANG_STRING,
				SymbolicHeap.$STRING_VALUE, conc_left, symb_left, conc_left);

		if (symb_right instanceof NonNullReference
				&& conc_right instanceof String) {
			NonNullReference non_null_symb_right = (NonNullReference) symb_right;
			String conc_right_str = (String) conc_right;

			StringValue right_expr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_right_str,
					non_null_symb_right, conc_right_str);

			if (left_expr.containsSymbolicVariable()
					|| right_expr.containsSymbolicVariable()) {
				int conV = res ? 1 : 0;
				StringBinaryComparison strBExpr = new StringBinaryComparison(left_expr,
						Operator.EQUALS, right_expr, (long) conV);
				return strBExpr;
			}

		}

		return this.getSymbIntegerRetVal();
	}
}
