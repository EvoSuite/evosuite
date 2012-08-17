package edu.uta.cse.dsc.vm2.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;

import edu.uta.cse.dsc.vm2.Function;
import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class L_ValueOf extends Function {

	private static final String VALUE_OF = "valueOf";

	public L_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_LONG, VALUE_OF, Types.L_TO_LONG);
	}

	private IntegerExpression bv64;

	@Override
	public void INVOKESTATIC() {
		bv64 = env.topFrame().operandStack.peekBv64();
	}

	@Override
	public void CALL_RESULT(Object conc_long) {
		NonNullReference symb_long = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_LONG, "$longValue", conc_long,
				symb_long, bv64);
	}

}
