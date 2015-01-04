package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class S_ShortValue extends SymbolicFunction {

	private static final String SHORT_VALUE = "shortValue";

	public S_ShortValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_SHORT, SHORT_VALUE, Types.TO_SHORT);
	}

	@Override
	public Object executeFunction() {

		NonNullReference symb_short = this.getSymbReceiver();
		Short conc_short = (Short) this.getConcReceiver();

		short conc_short_value = this.getConcShortRetVal();
		IntegerValue symb_short_value = env.heap.getField(
				Types.JAVA_LANG_SHORT, SymbolicHeap.$SHORT_VALUE, conc_short,
				symb_short, conc_short_value);
		return symb_short_value;
	}

}
