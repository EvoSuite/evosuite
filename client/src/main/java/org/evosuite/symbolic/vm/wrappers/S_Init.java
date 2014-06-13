package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class S_Init extends SymbolicFunction {

	public S_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_SHORT, Types.INIT, Types.S_TO_VOID);
	}

	@Override
	public Object executeFunction() {
		IntegerValue bv32 = this.getSymbIntegerArgument(0);
		NonNullReference symb_short = this.getSymbReceiver();
		env.heap.putField(Types.JAVA_LANG_SHORT, SymbolicHeap.$SHORT_VALUE,
				null/* conc_short */, symb_short, bv32);
		// return void
		return null;
	}

}
