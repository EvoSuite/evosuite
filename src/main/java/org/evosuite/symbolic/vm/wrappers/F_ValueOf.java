package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class F_ValueOf extends Function {

	private static final String VALUE_OF = "valueOf";

	public F_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_FLOAT, VALUE_OF, Types.F_TO_FLOAT);
	}

	private RealExpression fp32;

	@Override
	public void INVOKESTATIC() {
		fp32 = env.topFrame().operandStack.peekFp32();
	}

	@Override
	public void CALL_RESULT(Object conc_float) {
		NonNullReference symb_float = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_FLOAT, SymbolicHeap.$FLOAT_VALUE,
				conc_float, symb_float, fp32);
	}

}
