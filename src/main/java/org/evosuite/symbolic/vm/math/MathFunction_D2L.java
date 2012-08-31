package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class MathFunction_D2L extends Function {

	protected RealValue realExpression;

	public MathFunction_D2L(SymbolicEnvironment env, String name) {
		super(env, Types.JAVA_LANG_MATH, name,
				Types.D2L_DESCRIPTOR);
	}

	@Override
	public final void INVOKESTATIC() {
		realExpression = this.env.topFrame().operandStack.peekFp64();
	}

	@Override
	public final void CALL_RESULT(long res) {
		if (realExpression.containsSymbolicVariable()) {
			IntegerValue expr = executeFunction(res);
			replaceTopBv64(expr);
		}
	}

	protected abstract IntegerValue executeFunction(long res);
}
