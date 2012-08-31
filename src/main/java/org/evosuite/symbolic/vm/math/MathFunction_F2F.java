package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class MathFunction_F2F extends Function {

	protected RealValue realExpression;

	public MathFunction_F2F(SymbolicEnvironment env, String name) {
		super(env, Types.JAVA_LANG_MATH, name,
				Types.F2F_DESCRIPTOR);
	}

	@Override
	public final void INVOKESTATIC() {
		realExpression = this.env.topFrame().operandStack.peekFp32();
	}

	@Override
	public final void CALL_RESULT(float res) {
		if (realExpression.containsSymbolicVariable()) {
			RealValue acosExpr = executeFunction(res);
			replaceTopFp32(acosExpr);
		}
	}

	protected abstract RealValue executeFunction(float res);
}
