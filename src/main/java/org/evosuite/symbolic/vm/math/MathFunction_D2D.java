package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class MathFunction_D2D extends Function {

	protected RealValue realExpression;

	public MathFunction_D2D(SymbolicEnvironment env, String name) {
		super(env, Types.JAVA_LANG_MATH, name,
				Types.D2D_DESCRIPTOR);
	}

	@Override
	public final void INVOKESTATIC() {
		realExpression = this.env.topFrame().operandStack.peekFp64();
	}

	@Override
	public final void CALL_RESULT(double res) {
		if (realExpression.containsSymbolicVariable()) {
			RealValue acosExpr = executeFunction(res);
			replaceTopFp64(acosExpr);
		}
	}

	protected abstract RealValue executeFunction(double res);
}
