package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class B_ValueOf extends SymbolicFunction {

	private static final String VALUE_OF = "valueOf";

	public B_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BYTE, VALUE_OF, Types.B_TO_BYTE);
	}

	@Override
	public Object executeFunction() {
		IntegerValue int_value = this.getSymbIntegerArgument(0);

		NonNullReference symb_byte = (NonNullReference) this.getSymbRetVal();
		Byte conc_byte = (Byte) this.getConcRetVal();

		env.heap.putField(Types.JAVA_LANG_BYTE, SymbolicHeap.$BYTE_VALUE,
				conc_byte, symb_byte, int_value);

		return symb_byte;
	}

}
