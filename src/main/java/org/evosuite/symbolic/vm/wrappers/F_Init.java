package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class F_Init extends Function {


	public F_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_FLOAT, Types.INIT, Types.F_TO_VOID);
	}

	private RealExpression fp32;

	@Override
	public void INVOKESPECIAL() {
		fp32 = env.topFrame().operandStack.peekFp32();
	}

	@Override
	public void CALL_RESULT() {
		NonNullReference symb_float = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_FLOAT, SymbolicHeap.$FLOAT_VALUE,
				null/* conc_float */, symb_float, fp32);
	}

}
