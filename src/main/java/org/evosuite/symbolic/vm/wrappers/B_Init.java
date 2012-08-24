package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class B_Init extends Function {

	private IntegerExpression bv32;

	public B_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BYTE, Types.INIT, Types.B_TO_VOID);
	}

	@Override
	public void INVOKESPECIAL() {
		bv32 = env.topFrame().operandStack.peekBv32();
	}

	@Override
	public void CALL_RESULT() {
		NonNullReference symb_byte = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_BYTE, SymbolicHeap.$BYTE_VALUE,
				null/* conc_integer */, symb_byte, bv32);
	}

}
