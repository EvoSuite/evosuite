package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.bv.StringMultipleComparison;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class StartsWith extends SymbolicFunction {

	private static final String STARTS_WITH = "startsWith";

	public StartsWith(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING, STARTS_WITH,
				Types.STR_INT_TO_BOOL_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		// receiver
		NonNullReference symb_receiver = this.getSymbReceiver();
		String conc_receiver = (String) this.getConcReceiver();
		// prefix argument
		Reference symb_prefix = this.getSymbArgument(0);
		String conc_prefix = (String) this.getConcArgument(0);
		// toffset argument
		IntegerValue offsetExpr = this.getSymbIntegerArgument(1);

		// return value
		boolean res = this.getConcBooleanRetVal();

		StringValue stringReceiverExpr = env.heap.getField(
				Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
				conc_receiver, symb_receiver, conc_receiver);

		if (symb_prefix instanceof NonNullReference) {
			NonNullReference non_null_symb_prefix = (NonNullReference) symb_prefix;

			StringValue prefixExpr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_prefix,
					non_null_symb_prefix, conc_prefix);

			if (stringReceiverExpr.containsSymbolicVariable()
					|| prefixExpr.containsSymbolicVariable()
					|| offsetExpr.containsSymbolicVariable()) {
				int conV = res ? 1 : 0;

				StringMultipleComparison strTExpr = new StringMultipleComparison(
						stringReceiverExpr, Operator.STARTSWITH, prefixExpr,
						new ArrayList<Expression<?>>(Collections
								.singletonList(offsetExpr)),
						(long) conV);

				return strTExpr;
			}

		}
		return this.getSymbIntegerRetVal();
	}

}
