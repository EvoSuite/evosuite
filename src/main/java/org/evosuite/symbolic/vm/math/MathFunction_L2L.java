package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class MathFunction_L2L extends Function {

	protected IntegerValue integerExpression;

	public MathFunction_L2L(SymbolicEnvironment env, String name) {
		super(env, Types.JAVA_LANG_MATH, name,
				Types.L2L_DESCRIPTOR);
	}

	@Override
	public final void INVOKESTATIC() {
		integerExpression = this.env.topFrame().operandStack.peekBv64();
	}

	@Override
	public final void CALL_RESULT(long res) {
		if (integerExpression.containsSymbolicVariable()) {
			IntegerValue acosExpr = executeFunction(res);
			replaceTopBv64(acosExpr);
		}
	}

	protected abstract IntegerValue executeFunction(long res);
}
