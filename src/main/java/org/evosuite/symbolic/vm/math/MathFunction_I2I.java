package org.evosuite.symbolic.vm.math;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class MathFunction_I2I extends Function {

	protected IntegerValue integerExpression;

	public MathFunction_I2I(SymbolicEnvironment env, String name) {
		super(env, Types.JAVA_LANG_MATH, name,
				Types.I2I_DESCRIPTOR);
	}

	@Override
	public final void INVOKESTATIC() {
		integerExpression = this.env.topFrame().operandStack.peekBv32();
	}

	@Override
	public final void CALL_RESULT(int res) {
		if (integerExpression.containsSymbolicVariable()) {
			IntegerValue acosExpr = executeFunction(res);
			replaceTopBv32(acosExpr);
		}
	}

	protected abstract IntegerValue executeFunction(int res);
}
