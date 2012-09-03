package org.evosuite.symbolic.vm.math;

import java.util.Iterator;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class MathFunction_LL2L extends Function {

	protected IntegerValue left;
	protected IntegerValue right;

	public MathFunction_LL2L(SymbolicEnvironment env, String name) {
		super(env, Types.JAVA_LANG_MATH, name,
				Types.LL2L_DESCRIPTOR);
	}

	@Override
	public final void INVOKESTATIC() {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		right = bv64(it.next());
		left = bv64(it.next());
	}

	@Override
	public final void CALL_RESULT(long res) {
		if (left.containsSymbolicVariable() || right.containsSymbolicVariable()) {
			IntegerValue ret_val = executeFunction(res);
			replaceTopBv64(ret_val);
		}
	}

	protected abstract IntegerValue executeFunction(long res);
}
