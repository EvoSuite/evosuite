package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class J_LongValue extends SymbolicFunction {

	private static final String LONG_VALUE = "longValue";

	public J_LongValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_LONG, LONG_VALUE, Types.TO_LONG);
	}

	@Override
	public Object executeFunction() {
		NonNullReference symb_long = this.getSymbReceiver();
		Long conc_long = (Long) this.getConcReceiver();

		long conc_long_value = this.getConcLongRetVal();
		IntegerValue symb_long_value = env.heap
				.getField(Types.JAVA_LANG_LONG, SymbolicHeap.$LONG_VALUE,
						conc_long, symb_long, conc_long_value);
		return symb_long_value;
	}

}
