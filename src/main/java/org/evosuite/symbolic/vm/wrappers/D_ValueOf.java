package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class D_ValueOf extends Function {

	private static final String VALUE_OF = "valueOf";

	public D_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_DOUBLE, VALUE_OF, Types.D_TO_DOUBLE);
	}

	private RealValue fp64;

	@Override
	public void INVOKESTATIC() {
		fp64 = env.topFrame().operandStack.peekFp64();
	}

	@Override
	public void CALL_RESULT(Object conc_double) {
		NonNullReference symb_double = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_DOUBLE, SymbolicHeap.$DOUBLE_VALUE,
				conc_double, symb_double, fp64);
	}

}
