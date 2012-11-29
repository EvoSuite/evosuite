package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class J_Init extends SymbolicFunction {

	public J_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_LONG, Types.INIT, Types.J_TO_VOID);
	}

	@Override
	public Object executeFunction() {
		IntegerValue bv64 = this.getSymbIntegerArgument(0);
		NonNullReference symb_long = this.getSymbReceiver();
		env.heap.putField(Types.JAVA_LANG_LONG, SymbolicHeap.$LONG_VALUE,
				null/* conc_long */, symb_long, bv64);
		// return void
		return null;
	}

}
