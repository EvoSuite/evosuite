package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class B_Init extends SymbolicFunction {

	public B_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BYTE, Types.INIT, Types.B_TO_VOID);
	}

	@Override
	public Object executeFunction() {
		IntegerValue bv32 = this.getSymbIntegerArgument(0);
		NonNullReference symb_byte = (NonNullReference) this.getSymbReceiver();
		env.heap.putField(Types.JAVA_LANG_BYTE, SymbolicHeap.$BYTE_VALUE,
				null/* conc_integer */, symb_byte, bv32);

		// return void
		return null;
	}

}
