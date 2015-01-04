package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class F_FloatValue extends SymbolicFunction {

	private static final String FLOAT_VALUE = "floatValue";

	public F_FloatValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_FLOAT, FLOAT_VALUE, Types.TO_FLOAT);
	}

	@Override
	public Object executeFunction() {
		NonNullReference symb_float = this.getSymbReceiver();
		Float conc_float = (Float) this.getConcReceiver();

		float conc_float_value = this.getConcFloatRetVal();

		RealValue symb_int_value = env.heap.getField(Types.JAVA_LANG_FLOAT,
				SymbolicHeap.$FLOAT_VALUE, conc_float, symb_float,
				conc_float_value);

		return symb_int_value;
	}

}
