package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class S_ValueOf extends SymbolicFunction {

	private static final String VALUE_OF = "valueOf";

	public S_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_SHORT, VALUE_OF, Types.S_TO_SHORT);
	}

	@Override
	public Object executeFunction() {
		IntegerValue int_value = this.getSymbIntegerArgument(0);
		NonNullReference symb_short = (NonNullReference) this.getSymbRetVal();
		Short conc_short = (Short) this.getConcRetVal();
		env.heap.putField(Types.JAVA_LANG_SHORT, SymbolicHeap.$SHORT_VALUE,
				conc_short, symb_short, int_value);
		return symb_short;
	}

}
