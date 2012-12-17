package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class D_DoubleValue extends SymbolicFunction {

	private static final String DOUBLE_VALUE = "doubleValue";

	public D_DoubleValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_DOUBLE, DOUBLE_VALUE, Types.TO_DOUBLE);
	}

	@Override
	public Object executeFunction() {

		NonNullReference symb_double = this.getSymbReceiver();
		Double conc_double = (Double) this.getConcReceiver();
		double conc_double_value = this.getConcDoubleRetVal();

		RealValue symb_int_value = env.heap.getField(Types.JAVA_LANG_DOUBLE,
				SymbolicHeap.$DOUBLE_VALUE, conc_double, symb_double,
				conc_double_value);

		return symb_int_value;
	}

}
