package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Z_BooleanValue extends SymbolicFunction {

	private static final String BOOLEAN_VALUE = "booleanValue";

	public Z_BooleanValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BOOLEAN, BOOLEAN_VALUE, Types.TO_BOOLEAN);
	}

	@Override
	public Object executeFunction() {
		NonNullReference symb_boolean = this.getSymbReceiver();
		Boolean conc_boolean = (Boolean) this.getConcReceiver();
		boolean conc_boolean_value = this.getConcBooleanRetVal();
		IntegerValue symb_boolean_value = env.heap.getField(
				Types.JAVA_LANG_BOOLEAN, SymbolicHeap.$BOOLEAN_VALUE,
				conc_boolean, symb_boolean, conc_boolean_value ? 1 : 0);
		return symb_boolean_value;
	}

}
