package edu.uta.cse.dsc.vm2.wrappers;

import org.evosuite.symbolic.expr.RealExpression;

import edu.uta.cse.dsc.vm2.Function;
import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class F_ValueOf extends Function {

	private static final String VALUE_OF = "valueOf";

	public F_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_FLOAT, VALUE_OF, Types.F_TO_FLOAT);
	}

	private RealExpression fp32;

	@Override
	public void INVOKESTATIC() {
		fp32 = env.topFrame().operandStack.peekFp32();
	}

	@Override
	public void CALL_RESULT(Object conc_float) {
		NonNullReference symb_float = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_FLOAT, "$floatValue", conc_float,
				symb_float, fp32);
	}

}
