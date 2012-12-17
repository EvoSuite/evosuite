package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class F_ValueOf extends SymbolicFunction {

	private static final String VALUE_OF = "valueOf";

	public F_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_FLOAT, VALUE_OF, Types.F_TO_FLOAT);
	}

	@Override
	public Object executeFunction() {
		RealValue real_value = this.getSymbRealArgument(0);
		NonNullReference symb_float = (NonNullReference) this.getSymbRetVal();
		Float conc_float = (Float) this.getConcRetVal();
		env.heap.putField(Types.JAVA_LANG_FLOAT, SymbolicHeap.$FLOAT_VALUE,
				conc_float, symb_float, real_value);
		return symb_float;
	}

}
