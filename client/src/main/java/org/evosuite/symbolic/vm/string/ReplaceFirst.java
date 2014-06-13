package org.evosuite.symbolic.vm.string;

import java.util.ArrayList;
import java.util.Collections;

import org.evosuite.symbolic.expr.Expression;
import org.evosuite.symbolic.expr.Operator;
import org.evosuite.symbolic.expr.str.StringMultipleExpression;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class ReplaceFirst extends SymbolicFunction {

	private static final String REPLACE_FIRST = "replaceFirst";

	public ReplaceFirst(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING, REPLACE_FIRST,
				Types.STR_STR_TO_STR_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		// receiver
		NonNullReference symb_receiver = this.getSymbReceiver();
		String conc_receiver = (String) this.getConcReceiver();
		// regex argument
		Reference symb_regex = this.getSymbArgument(0);
		String conc_regex = (String) this.getConcArgument(0);
		// replacement argument
		Reference symb_replacement = this.getSymbArgument(1);
		String conc_replacement = (String) this.getConcArgument(1);
		// return value
		String conc_ret_val = (String) this.getConcRetVal();
		Reference symb_ret_val = this.getSymbRetVal();

		StringValue stringReceiverExpr = env.heap.getField(
				Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
				conc_receiver, symb_receiver, conc_receiver);

		if (symb_regex instanceof NonNullReference
				&& symb_replacement instanceof NonNullReference) {

			NonNullReference non_null_symb_regex = (NonNullReference) symb_regex;
			StringValue regexExpr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_regex,
					non_null_symb_regex, conc_regex);

			NonNullReference non_null_symb_replacement = (NonNullReference) symb_replacement;
			StringValue replacementExpr = env.heap.getField(
					Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
					conc_replacement, non_null_symb_replacement,
					conc_replacement);

			if (symb_ret_val instanceof NonNullReference) {
				NonNullReference non_null_symb_ret_val = (NonNullReference) symb_ret_val;

				StringMultipleExpression symb_value = new StringMultipleExpression(
						stringReceiverExpr, Operator.REPLACEFIRST, regexExpr,
						new ArrayList<Expression<?>>(Collections
								.singletonList(replacementExpr)),
						conc_ret_val);

				env.heap.putField(Types.JAVA_LANG_STRING,
						SymbolicHeap.$STRING_VALUE, conc_ret_val,
						non_null_symb_ret_val, symb_value);
			}

		}
		return symb_ret_val;
	}

}
