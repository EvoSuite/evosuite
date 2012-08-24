package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class D_Init extends Function {

	public D_Init(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_DOUBLE, Types.INIT, Types.D_TO_VOID);
	}

	private RealExpression fp64;

	@Override
	public void INVOKESPECIAL() {
		fp64 = env.topFrame().operandStack.peekFp64();
	}

	@Override
	public void CALL_RESULT() {
		NonNullReference symb_double = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_DOUBLE, SymbolicHeap.$DOUBLE_VALUE,
				null/* conc_double */, symb_double, fp64);
	}

}
