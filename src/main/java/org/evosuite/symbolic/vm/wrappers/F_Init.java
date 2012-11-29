package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class F_Init extends SymbolicFunction {

	public F_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_FLOAT, Types.INIT, Types.F_TO_VOID);
	}

	@Override
	public Object executeFunction() {

		RealValue fp32 = this.getSymbRealArgument(0);
		NonNullReference symb_float = this.getSymbReceiver();
		env.heap.putField(Types.JAVA_LANG_FLOAT, SymbolicHeap.$FLOAT_VALUE,
				null/* conc_float */, symb_float, fp32);
		// return void
		return null;
	}

}
