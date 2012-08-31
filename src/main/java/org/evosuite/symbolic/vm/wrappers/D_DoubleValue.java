package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;


public final class D_DoubleValue extends Function {

	private static final String DOUBLE_VALUE = "doubleValue";
	private NonNullReference symb_double;
	private Double conc_double;

	public D_DoubleValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_DOUBLE, DOUBLE_VALUE, Types.TO_DOUBLE);
	}

	@Override
	public void INVOKEVIRTUAL(Object conc_double) {
		if (conc_double == null)
			return;

		symb_double = (NonNullReference) this.env.topFrame().operandStack
				.peekRef();
		this.conc_double = (Double) conc_double;
	}

	@Override
	public void CALL_RESULT(double conc_double_value) {
		RealValue symb_int_value = env.heap.getField(
				Types.JAVA_LANG_DOUBLE, SymbolicHeap.$DOUBLE_VALUE, conc_double,
				symb_double, conc_double_value);

		replaceTopFp64(symb_int_value);
	}

}
