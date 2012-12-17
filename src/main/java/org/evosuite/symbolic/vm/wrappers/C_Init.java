package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class C_Init extends SymbolicFunction {

	public C_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_CHARACTER, Types.INIT, Types.C_TO_VOID);
	}

	@Override
	public Object executeFunction() {
		NonNullReference symb_character = this.getSymbReceiver();
		IntegerValue bv32 = this.getSymbIntegerArgument(0);

		env.heap.putField(Types.JAVA_LANG_CHARACTER, SymbolicHeap.$CHAR_VALUE,
				null/* conc_character */, symb_character, bv32);

		// return void
		return null;
	}

}
