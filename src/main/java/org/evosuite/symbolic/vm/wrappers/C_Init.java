package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class C_Init extends Function {

	public C_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_CHARACTER, Types.INIT, Types.C_TO_VOID);
	}

	private IntegerExpression bv32;

	@Override
	public void INVOKESPECIAL() {
		bv32 = env.topFrame().operandStack.peekBv32();
	}

	@Override
	public void CALL_RESULT() {
		NonNullReference symb_character = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_CHARACTER, SymbolicHeap.$CHAR_VALUE,
				null/* conc_character */, symb_character, bv32);
	}

}
