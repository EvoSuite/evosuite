package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class B_ByteValue extends Function {

	private NonNullReference symb_byte;
	private Byte conc_byte;

	private static final String BYTE_VALUE = "byteValue";

	public B_ByteValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BYTE, BYTE_VALUE, Types.TO_BYTE);
	}

	@Override
	public void INVOKEVIRTUAL(Object conc_byte) {
		if (conc_byte == null)
			return;

		symb_byte = (NonNullReference) this.env.topFrame().operandStack
				.peekRef();
		this.conc_byte = (Byte) conc_byte;
	}

	@Override
	public void CALL_RESULT(int conc_byte_value) {
		IntegerExpression symb_byte_value = env.heap.getField(
				Types.JAVA_LANG_BYTE, SymbolicHeap.$BYTE_VALUE, conc_byte,
				symb_byte, conc_byte_value);

		replaceTopBv32(symb_byte_value);
	}

}
