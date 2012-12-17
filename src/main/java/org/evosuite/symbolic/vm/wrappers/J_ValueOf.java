package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class J_ValueOf extends SymbolicFunction {

	private static final String VALUE_OF = "valueOf";

	public J_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_LONG, VALUE_OF, Types.J_TO_LONG);
	}

	@Override
	public Object executeFunction() {
		IntegerValue int_value = this.getSymbIntegerArgument(0);
		NonNullReference symb_long = (NonNullReference) this.getSymbRetVal();
		Long conc_long = (Long) this.getConcRetVal();
		env.heap.putField(Types.JAVA_LANG_LONG, SymbolicHeap.$LONG_VALUE,
				conc_long, symb_long, int_value);

		return symb_long;
	}

}
