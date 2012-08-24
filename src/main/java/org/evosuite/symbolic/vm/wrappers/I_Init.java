package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class I_Init extends Function {

	private IntegerExpression bv32;

	public I_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_INTEGER, Types.INIT, Types.I_TO_VOID);
	}

	@Override
	public void INVOKESPECIAL() {
		bv32 = env.topFrame().operandStack.peekBv32();
	}

	@Override
	public void CALL_RESULT() {
		NonNullReference symb_integer = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_INTEGER, SymbolicHeap.$INT_VALUE,
				null/* conc_integer */, symb_integer, bv32);
	}

}
