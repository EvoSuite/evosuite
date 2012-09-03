package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class S_ValueOf extends Function {

	private static final String VALUE_OF = "valueOf";

	public S_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_SHORT, VALUE_OF, Types.S_TO_SHORT);
	}

	private IntegerValue bv32;

	@Override
	public void INVOKESTATIC() {
		bv32 = env.topFrame().operandStack.peekBv32();
	}

	@Override
	public void CALL_RESULT(Object conc_short) {
		NonNullReference symb_short = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_SHORT, SymbolicHeap.$SHORT_VALUE,
				conc_short, symb_short, bv32);
	}

}
