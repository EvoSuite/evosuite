package org.evosuite.symbolic.vm.string.builder;

import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;
import org.evosuite.symbolic.vm.string.Types;

public final class SB_ToString extends StringBuilderFunction {

	private static final String FUNCTION_NAME = "toString";

	public SB_ToString(SymbolicEnvironment env) {
		super(env, FUNCTION_NAME, Types.TO_STR_DESCRIPTOR);
	}

	private StringBuilder conc_str_builder;

	@Override
	protected void INVOKEVIRTUAL_StringBuilder(StringBuilder conc_receiver) {
		if (conc_receiver == null)
			return;
		// the reference can not be null at this point
		symb_receiver = (NonNullReference) this.env.topFrame().operandStack
				.peekRef();

		conc_str_builder = conc_receiver;

	}

	@Override
	public void CALL_RESULT(Object res) {

		if (res != null) {
			StringValue symb_value = env.heap.getField(
					JAVA_LANG_STRING_BUILDER,
					SymbolicHeap.$STRING_BUILDER_CONTENTS, conc_str_builder,
					symb_receiver, conc_str_builder.toString());

			NonNullReference symb_receiver = (NonNullReference) env.topFrame().operandStack
					.peekRef();
			String conc_receiver = (String) res;
			env.heap.putField(Types.JAVA_LANG_STRING,
					SymbolicHeap.$STRING_VALUE, conc_receiver, symb_receiver,
					symb_value);
		}

	}
}
