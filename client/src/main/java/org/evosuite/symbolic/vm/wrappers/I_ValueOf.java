package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class I_ValueOf extends SymbolicFunction {

	private static final String VALUE_OF = "valueOf";

	public I_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_INTEGER, VALUE_OF, Types.I_TO_INTEGER);
	}

	@Override
	public Object executeFunction() {
		IntegerValue int_value = this.getSymbIntegerArgument(0);
		NonNullReference symb_integer = (NonNullReference) this.getSymbRetVal();
		Integer conc_integer = (Integer) this.getConcRetVal();
		env.heap.putField(Types.JAVA_LANG_INTEGER, SymbolicHeap.$INT_VALUE,
				conc_integer, symb_integer, int_value);
		return symb_integer;
	}

}
