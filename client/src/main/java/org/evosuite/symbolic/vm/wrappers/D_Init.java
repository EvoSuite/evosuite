package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class D_Init extends SymbolicFunction {

	public D_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_DOUBLE, Types.INIT, Types.D_TO_VOID);
	}

	@Override
	public Object executeFunction() {
		RealValue fp64 = this.getSymbRealArgument(0);
		NonNullReference symb_double = this.getSymbReceiver();
		env.heap.putField(Types.JAVA_LANG_DOUBLE, SymbolicHeap.$DOUBLE_VALUE,
				null/* conc_double */, symb_double, fp64);
		// return voids
		return null;
	}

}
