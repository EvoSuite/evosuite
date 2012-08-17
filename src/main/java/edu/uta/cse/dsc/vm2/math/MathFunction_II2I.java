package edu.uta.cse.dsc.vm2.math;

import java.util.Iterator;

import org.evosuite.symbolic.expr.IntegerExpression;

import edu.uta.cse.dsc.vm2.Function;
import edu.uta.cse.dsc.vm2.Operand;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public abstract class MathFunction_II2I extends Function {

	protected IntegerExpression left;
	protected IntegerExpression right;

	public MathFunction_II2I(SymbolicEnvironment env, String name) {
		super(env, Types.JAVA_LANG_MATH, name,
				Types.II2I_DESCRIPTOR);
	}

	@Override
	public final void INVOKESTATIC() {
		Iterator<Operand> it = env.topFrame().operandStack.iterator();
		right = bv32(it.next());
		left = bv32(it.next());
	}

	@Override
	public final void CALL_RESULT(int res) {
		if (left.containsSymbolicVariable() || right.containsSymbolicVariable()) {
			IntegerExpression ret_val = executeFunction(res);
			replaceTopBv32(ret_val);
		}
	}

	protected abstract IntegerExpression executeFunction(int res);
}
