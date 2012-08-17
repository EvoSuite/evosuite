package edu.uta.cse.dsc.vm2.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;

import edu.uta.cse.dsc.vm2.Function;
import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class I_IntValue extends Function {

	private static final String INT_VALUE = "intValue";
	private NonNullReference symb_integer;
	private Integer conc_integer;

	public I_IntValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_INTEGER, INT_VALUE, Types.TO_INT);
	}

	@Override
	public void INVOKEVIRTUAL(Object conc_integer) {
		if (conc_integer == null)
			return;

		symb_integer = (NonNullReference) this.env.topFrame().operandStack
				.peekRef();
		this.conc_integer = (Integer) conc_integer;
	}

	@Override
	public void CALL_RESULT(int conc_int_value) {
		IntegerExpression symb_int_value = env.heap.getField(
				Types.JAVA_LANG_INTEGER, "$intValue", conc_integer, symb_integer,
				conc_int_value);

		replaceTopBv32(symb_int_value);
	}

}
