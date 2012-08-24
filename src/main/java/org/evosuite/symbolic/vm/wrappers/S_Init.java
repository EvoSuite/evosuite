package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class S_Init extends Function {

	public S_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_SHORT, Types.INIT, Types.S_TO_VOID);
	}

	private IntegerExpression bv32;

	@Override
	public void INVOKESPECIAL() {
		bv32 = env.topFrame().operandStack.peekBv32();
	}

	@Override
	public void CALL_RESULT() {
		NonNullReference symb_short = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_SHORT, SymbolicHeap.$SHORT_VALUE,
				null/* conc_short */, symb_short, bv32);
	}

}
