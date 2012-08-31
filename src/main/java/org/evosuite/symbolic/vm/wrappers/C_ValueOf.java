package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class C_ValueOf extends Function {

	private static final String VALUE_OF = "valueOf";

	public C_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_CHARACTER, VALUE_OF, Types.C_TO_CHARACTER);
	}

	private IntegerValue bv32;

	@Override
	public void INVOKESTATIC() {
		bv32 = env.topFrame().operandStack.peekBv32();
	}

	@Override
	public void CALL_RESULT(Object conc_character) {
		NonNullReference symb_character = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_CHARACTER, SymbolicHeap.$CHAR_VALUE,
				conc_character, symb_character, bv32);
	}

}
