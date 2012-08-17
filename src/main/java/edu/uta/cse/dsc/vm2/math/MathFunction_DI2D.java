package edu.uta.cse.dsc.vm2.math;

import java.util.Iterator;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.expr.RealExpression;

import edu.uta.cse.dsc.vm2.Function;
import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class MathFunction_DI2D extends Function {

	protected RealExpression left;
	protected IntegerExpression right;

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
			RealExpression ret_val = executeFunction(res);
			replaceTopFp64(ret_val);
		}
	}

	protected abstract RealExpression executeFunction(double res);
}
