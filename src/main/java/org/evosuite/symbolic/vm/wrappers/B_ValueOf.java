package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class B_ValueOf extends Function {

	private static final String VALUE_OF = "valueOf";

	public B_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BYTE, VALUE_OF, Types.B_TO_BYTE);
	}

	private IntegerExpression bv32;

	@Override
	public void INVOKESTATIC() {
		bv32 = env.topFrame().operandStack.peekBv32();
	}

	@Override
	public void CALL_RESULT(Object conc_byte) {
		NonNullReference symb_byte = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_BYTE, SymbolicHeap.$BYTE_VALUE,
				conc_byte, symb_byte, bv32);
	}

}
