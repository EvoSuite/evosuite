package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class S_ShortValue extends Function {

	private NonNullReference symb_short;
	private Short conc_short;

	private static final String SHORT_VALUE = "shortValue";

	public S_ShortValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_SHORT, SHORT_VALUE, Types.TO_SHORT);
	}

	@Override
	public void INVOKEVIRTUAL(Object conc_short) {
		if (conc_short == null)
			return;

		symb_short = (NonNullReference) this.env.topFrame().operandStack
				.peekRef();
		this.conc_short = (Short) conc_short;
	}

	@Override
	public void CALL_RESULT(int conc_short_value) {
		IntegerExpression symb_short_value = env.heap.getField(
				Types.JAVA_LANG_SHORT, SymbolicHeap.$SHORT_VALUE, conc_short, symb_short,
				conc_short_value);

		replaceTopBv32(symb_short_value);
	}

}
