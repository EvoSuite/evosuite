package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class D_ValueOf extends SymbolicFunction {

	private static final String VALUE_OF = "valueOf";

	public D_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_DOUBLE, VALUE_OF, Types.D_TO_DOUBLE);
	}

	@Override
	public Object executeFunction() {
		RealValue real_value = this.getSymbRealArgument(0);
		NonNullReference symb_double = (NonNullReference) this.getSymbRetVal();
		Double conc_double = (Double) this.getConcRetVal();
		env.heap.putField(Types.JAVA_LANG_DOUBLE, SymbolicHeap.$DOUBLE_VALUE,
				conc_double, symb_double, real_value);

		return symb_double;
	}

}
