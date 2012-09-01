package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Z_Init extends Function {

	public Z_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BOOLEAN, Types.INIT, Types.Z_TO_VOID);
	}

	private IntegerValue bv32;

	@Override
	public void INVOKESPECIAL() {
		bv32 = env.topFrame().operandStack.peekBv32();
	}

	@Override
	public void CALL_RESULT() {
		NonNullReference symb_boolean = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_BOOLEAN, SymbolicHeap.$BOOLEAN_VALUE,
				null/* conc_boolean */, symb_boolean, bv32);
	}

}
