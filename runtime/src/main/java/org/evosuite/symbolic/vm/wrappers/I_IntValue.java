package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class I_IntValue extends SymbolicFunction {

	private static final String INT_VALUE = "intValue";

	public I_IntValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_INTEGER, INT_VALUE, Types.TO_INT);
	}

	@Override
	public Object executeFunction() {
		NonNullReference symb_integer = this.getSymbReceiver();
		Integer conc_integer = (Integer) this.getConcReceiver();
		int conc_int_value = this.getConcIntRetVal();

		IntegerValue symb_int_value = env.heap.getField(
				Types.JAVA_LANG_INTEGER, SymbolicHeap.$INT_VALUE, conc_integer,
				symb_integer, conc_int_value);

		return symb_int_value;
	}

}
