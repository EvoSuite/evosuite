package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class J_Init extends Function {

	public J_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_LONG, Types.INIT, Types.J_TO_VOID);
	}

	private IntegerExpression bv64;

	@Override
	public void INVOKESPECIAL() {
		bv64 = env.topFrame().operandStack.peekBv64();
	}

	@Override
	public void CALL_RESULT() {
		NonNullReference symb_long = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_LONG, SymbolicHeap.$LONG_VALUE,
				null/* conc_long */, symb_long, bv64);
	}

}
