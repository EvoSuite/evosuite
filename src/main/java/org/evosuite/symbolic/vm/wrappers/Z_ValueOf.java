package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Z_ValueOf extends Function {

	private static final String VALUE_OF = "valueOf";

	public Z_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BOOLEAN, VALUE_OF, Types.Z_TO_BOOLEAN);
	}

	private IntegerValue bv32;

	@Override
	public void INVOKESTATIC() {
		bv32 = env.topFrame().operandStack.peekBv32();
	}

	@Override
	public void CALL_RESULT(Object conc_boolean) {
		NonNullReference symb_boolean = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_BOOLEAN, SymbolicHeap.$BOOLEAN_VALUE,
				conc_boolean, symb_boolean, bv32);
	}

}
