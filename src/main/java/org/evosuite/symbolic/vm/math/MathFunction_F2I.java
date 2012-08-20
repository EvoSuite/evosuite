package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class MathFunction_F2I extends Function {

	protected RealExpression realExpression;

	public MathFunction_F2I(SymbolicEnvironment env, String name) {
		super(env, Types.JAVA_LANG_MATH, name,
				Types.F2I_DESCRIPTOR);
	}

	@Override
	public final void INVOKESTATIC() {
		realExpression = this.env.topFrame().operandStack.peekFp32();
	}

	@Override
	public final void CALL_RESULT(int res) {
		if (realExpression.containsSymbolicVariable()) {
			IntegerExpression expr = executeFunction(res);
			replaceTopBv32(expr);
		}
	}

	protected abstract IntegerExpression executeFunction(int res);
}
