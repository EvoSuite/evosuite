package org.evosuite.symbolic.vm.string.tokenizer;

import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.expr.token.NewTokenizerExpr;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

public final class StringTokenizer_Init extends SymbolicFunction {

	private static final String INIT = "<init>";

	public StringTokenizer_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_UTIL_STRING_TOKENIZER, INIT,
				Types.STR_STR_TO_VOID_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		// symbolic receiver (new object)
		NonNullReference symb_str_tokenizer = (NonNullReference) this
				.getSymbReceiver();

		// string argument
		String conc_str = (String) this.getConcArgument(0);
		Reference symb_str = this.getSymbArgument(0);

		// delim argument
		String conc_delim = (String) this.getConcArgument(1);
		Reference symb_delim = this.getSymbArgument(1);

		if (symb_str instanceof NonNullReference
				&& symb_delim instanceof NonNullReference) {
			NonNullReference non_null_symb_string = (NonNullReference) symb_str;
			assert conc_str != null;

			StringValue strExpr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_str, non_null_symb_string,
					conc_str);

			NonNullReference non_null_symb_delim = (NonNullReference) symb_delim;
			assert conc_delim != null;

			StringValue delimExpr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_delim,
					non_null_symb_delim, conc_delim);

			NewTokenizerExpr newTokenizerExpr = new NewTokenizerExpr(strExpr,
					delimExpr);

			// update symbolic heap
			env.heap.putField(Types.JAVA_UTIL_STRING_TOKENIZER,
					SymbolicHeap.$STRING_TOKENIZER_VALUE, null,
					symb_str_tokenizer, newTokenizerExpr);
		}

		// constructor returns void
		return null;
	}
}
