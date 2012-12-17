package org.evosuite.symbolic.vm.string.builder;

import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

public final class StringBuilder_Init extends SymbolicFunction {

	private static final String INIT = "<init>";

	public StringBuilder_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING_BUILDER, INIT,
				Types.STR_TO_VOID_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {

		// symbolic receiver (new object)
		NonNullReference symb_str_builder = (NonNullReference) this
				.getSymbReceiver();

		// string argument
		String conc_str = (String) this.getConcArgument(0);
		Reference symb_str = this.getSymbArgument(0);

		if (symb_str instanceof NonNullReference) {
			NonNullReference non_null_symb_string = (NonNullReference) symb_str;
			assert conc_str != null;

			StringValue strExpr = env.heap.getField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_str, non_null_symb_string,
					conc_str);

			// update symbolic heap
			env.heap.putField(Types.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, null,
					symb_str_builder, strExpr);
		}

		// return void
		return null;
	}

}
