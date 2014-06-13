package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Z_ValueOf extends SymbolicFunction {

	private static final String VALUE_OF = "valueOf";

	public Z_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BOOLEAN, VALUE_OF, Types.Z_TO_BOOLEAN);
	}

	@Override
	public Object executeFunction() {
		IntegerValue int_value = this.getSymbIntegerArgument(0);
		NonNullReference symb_boolean = (NonNullReference) this.getSymbRetVal();
		Boolean conc_boolean = (Boolean) this.getConcRetVal();
		env.heap.putField(Types.JAVA_LANG_BOOLEAN, SymbolicHeap.$BOOLEAN_VALUE,
				conc_boolean, symb_boolean, int_value);
		return symb_boolean;
	}

}
