package org.evosuite.symbolic.vm.string.buffer;

import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class StringBuffer_ToString extends SymbolicFunction {

	private static final String TO_STRING = "toString";

	public StringBuffer_ToString(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_STRING_BUFFER, TO_STRING,
				Types.TO_STR_DESCRIPTOR);
	}

	@Override
	public Object executeFunction() {
		NonNullReference symb_str_buffer = this.getSymbReceiver();
		StringBuffer conc_str_buffer = (StringBuffer) this.getConcReceiver();

		// retrieve symbolic value from heap
		String conc_value = conc_str_buffer.toString();
		StringValue symb_value = env.heap.getField(
				Types.JAVA_LANG_STRING_BUFFER,
				SymbolicHeap.$STRING_BUFFER_CONTENTS, conc_str_buffer,
				symb_str_buffer, conc_value);

		String conc_ret_val = (String) this.getConcRetVal();
		NonNullReference symb_ret_val = (NonNullReference) this.getSymbRetVal();

		env.heap.putField(Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
				conc_ret_val, symb_ret_val, symb_value);

		// return symbolic value
		return symb_ret_val;
	}

}
