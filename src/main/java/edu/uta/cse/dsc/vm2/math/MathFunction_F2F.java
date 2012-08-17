package edu.uta.cse.dsc.vm2.math;

import org.evosuite.symbolic.expr.RealExpression;

import edu.uta.cse.dsc.vm2.Function;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class MathFunction_F2F extends Function {

	protected RealExpression realExpression;

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
			RealExpression acosExpr = executeFunction(res);
			replaceTopFp32(acosExpr);
		}
	}

	protected abstract RealExpression executeFunction(float res);
}
