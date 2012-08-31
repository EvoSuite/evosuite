package org.evosuite.symbolic.vm.math;

import java.util.Iterator;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.expr.fp.RealValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.Operand;
import org.evosuite.symbolic.vm.SymbolicEnvironment;


public abstract class MathFunction_DI2D extends Function {

	protected RealValue left;
	protected IntegerValue right;

	public MathFunction_DI2D(SymbolicEnvironment env, String name) {
		super(env, Types.JAVA_LANG_MATH, name,
				Types.DI2D_DESCRIPTOR);
	}

	@Override
	public final void INVOKESTATIC() {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		right = bv32(it.next());
		left = fp64(it.next());
	}

	

	@Override
	public final void CALL_RESULT(double res) {
		if (left.containsSymbolicVariable() || right.containsSymbolicVariable()) {
			RealValue ret_val = executeFunction(res);
			replaceTopFp64(ret_val);
		}
	}

	protected abstract RealValue executeFunction(double res);
}
