package edu.uta.cse.dsc.vm2.wrappers;

import org.evosuite.symbolic.expr.RealExpression;

import edu.uta.cse.dsc.vm2.Function;
import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class D_ValueOf extends Function {

	private static final String VALUE_OF = "valueOf";

	public D_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_DOUBLE, VALUE_OF, Types.D_TO_DOUBLE);
	}

	private RealExpression fp64;

	@Override
	public void INVOKESTATIC() {
		fp64 = env.topFrame().operandStack.peekFp64();
	}

	@Override
	public void CALL_RESULT(Object conc_double) {
		NonNullReference symb_double = (NonNullReference) env.topFrame().operandStack
				.peekRef();
		env.heap.putField(Types.JAVA_LANG_DOUBLE, "$doubleValue", conc_double,
				symb_double, fp64);
	}

}
