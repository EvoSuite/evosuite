package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Z_Init extends SymbolicFunction {

	public Z_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BOOLEAN, Types.INIT, Types.Z_TO_VOID);
	}

	@Override
	public Object executeFunction() {
		IntegerValue bv32 = this.getSymbIntegerArgument(0);
		NonNullReference symb_boolean = this.getSymbReceiver();
		env.heap.putField(Types.JAVA_LANG_BOOLEAN, SymbolicHeap.$BOOLEAN_VALUE,
				null/* conc_boolean */, symb_boolean, bv32);

		// return void
		return null;
	}

}
