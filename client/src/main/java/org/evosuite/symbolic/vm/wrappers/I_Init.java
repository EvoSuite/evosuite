package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class I_Init extends SymbolicFunction {

	public I_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_INTEGER, Types.INIT, Types.I_TO_VOID);
	}

	@Override
	public Object executeFunction() {
		IntegerValue bv32 = this.getSymbIntegerArgument(0);
		NonNullReference symb_integer = this.getSymbReceiver();
		env.heap.putField(Types.JAVA_LANG_INTEGER, SymbolicHeap.$INT_VALUE,
				null/* conc_integer */, symb_integer, bv32);

		// return void
		return null;
	}

}
