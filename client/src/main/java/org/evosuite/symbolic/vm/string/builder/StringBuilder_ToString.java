package org.evosuite.symbolic.vm.string.builder;

import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

public final class StringBuilder_ToString extends SymbolicFunction {

	private static final String TO_STRING = "toString";

	public StringBuilder_ToString(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING_BUILDER, TO_STRING,
				Types.TO_STR_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		NonNullReference symb_str_builder = (NonNullReference) this
				.getSymbReceiver();

		// receiver
		StringBuilder conc_str_builder = (StringBuilder) this.getConcReceiver();

		// return value
		String res = (String) this.getConcRetVal();

		if (res != null) {
			NonNullReference symb_ret_val = (NonNullReference) this
					.getSymbRetVal();

			StringValue symb_value = env.heap.getField(
					Types.JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_str_builder, conc_str_builder.toString());

			String conc_receiver = (String) res;
			env.heap.putField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_receiver, symb_ret_val,
					symb_value);
		}

		return this.getSymbRetVal();
	}
}
