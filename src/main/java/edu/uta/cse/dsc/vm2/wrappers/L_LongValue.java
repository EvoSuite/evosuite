package edu.uta.cse.dsc.vm2.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;

import edu.uta.cse.dsc.vm2.Function;
import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class L_LongValue extends Function {

	private static final String LONG_VALUE = "longValue";
	private NonNullReference symb_long;
	private Long conc_long;

	public L_LongValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_LONG, LONG_VALUE, Types.TO_LONG);
	}

	@Override
	public void INVOKEVIRTUAL(Object conc_long) {
		if (conc_long == null)
			return;

		symb_long = (NonNullReference) this.env.topFrame().operandStack
				.peekRef();
		this.conc_long = (Long) conc_long;
	}

	@Override
	public void CALL_RESULT(long conc_long_value) {
		IntegerExpression symb_long_value = env.heap.getField(
				Types.JAVA_LANG_LONG, "$longValue", conc_long, symb_long,
				conc_long_value);

		replaceTopBv64(symb_long_value);
	}

}
