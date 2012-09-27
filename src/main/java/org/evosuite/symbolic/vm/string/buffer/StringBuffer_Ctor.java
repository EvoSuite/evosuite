package org.evosuite.symbolic.vm.string.buffer;

import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.RFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public abstract class StringBuffer_Ctor extends RFunction {

	private static final String INIT = "<init>";

	public StringBuffer_Ctor(SymbolicEnvironment env, String desc) {
		super(env, Types.JAVA_LANG_STRING_BUFFER, INIT, desc);
	}

	public static final class StringBufferCtor_S extends StringBuffer_Ctor {

		public StringBufferCtor_S(SymbolicEnvironment env) {
			super(env, Types.STR_TO_VOID_DESCRIPTOR);

		}

		/**
		 * new StringBuffer(String)
		 */
		@Override
		public Object executeFunction() {
			NonNullReference symb_str_buffer = this.getSymbReceiver();
			NonNullReference symb_string = (NonNullReference) this
					.getSymbArgument(0);
			String conc_string = (String) this.getConcArgument(0);

			// get symbolic value for string argument
			StringValue string_value = this.env.heap.getField(
					Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
					conc_string, symb_string, conc_string);

			// update symbolic heap
			this.env.heap.putField(Types.JAVA_LANG_STRING_BUFFER,
					SymbolicHeap.$STRING_BUFFER_CONTENTS, null,
					symb_str_buffer, string_value);

			// return void
			return null;
		}

	}

}
