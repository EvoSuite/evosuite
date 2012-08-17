package edu.uta.cse.dsc.vm2.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;

import edu.uta.cse.dsc.vm2.Function;
import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class I_IntValue extends Function {

	private static final String FUNCTION_NAME = "intValue";
	private NonNullReference symb_integer;
	private Object conc_integer;

	public I_IntValue(SymbolicEnvironment env) {
		super(env, Integer.class.getName().replace(".", "/"), FUNCTION_NAME, Types.TO_INT);
	}

	@Override
	public void INVOKEVIRTUAL(Object conc_integer) {
		if (conc_integer == null)
			return;

		symb_integer = (NonNullReference) this.env.topFrame().operandStack
				.peekRef();
		this.conc_integer = conc_integer;
	}

	@Override
	public void CALL_RESULT(int conc_int_value) {
		IntegerExpression symb_int_value = env.heap.getField(
				"java.lang.Integer", "$intValue", conc_integer, symb_integer,
				conc_int_value);

		replaceTopBv32(symb_int_value);
	}

}
