package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class B_ByteValue extends SymbolicFunction {

	private static final String BYTE_VALUE = "byteValue";

	public B_ByteValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BYTE, BYTE_VALUE, Types.TO_BYTE);
	}

	@Override
	public Object executeFunction() {
		NonNullReference symb_byte = this.getSymbReceiver();
		Byte conc_byte = (Byte) this.getConcReceiver();

		int conc_byte_value = this.getConcByteRetVal();

		IntegerValue symb_byte_value = env.heap
				.getField(Types.JAVA_LANG_BYTE, SymbolicHeap.$BYTE_VALUE,
						conc_byte, symb_byte, conc_byte_value);

		return symb_byte_value;
	}

}
