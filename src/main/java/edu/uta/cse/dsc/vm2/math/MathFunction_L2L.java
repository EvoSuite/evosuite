package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.IntegerExpression;

import edu.uta.cse.dsc.vm2.Function;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class MathFunction_L2L extends Function {

	protected IntegerExpression integerExpression;

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
			IntegerExpression acosExpr = executeFunction(res);
			replaceTopBv64(acosExpr);
		}
	}

	protected abstract IntegerExpression executeFunction(long res);
}
