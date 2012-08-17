package edu.uta.cse.dsc.vm2.wrappers;

import org.evosuite.symbolic.expr.RealExpression;

import edu.uta.cse.dsc.vm2.Function;
import edu.uta.cse.dsc.vm2.NonNullReference;
import edu.uta.cse.dsc.vm2.SymbolicEnvironment;

public final class F_FloatValue extends Function {

	private static final String FLOAT_VALUE = "floatValue";
	private NonNullReference symb_float;
	private Float conc_float;

	public F_FloatValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_FLOAT, FLOAT_VALUE, Types.TO_FLOAT);
	}

	@Override
	public void INVOKEVIRTUAL(Object conc_float) {
		if (conc_float == null)
			return;

		symb_float = (NonNullReference) this.env.topFrame().operandStack
				.peekRef();
		this.conc_float = (Float) conc_float;
	}

	@Override
	public void CALL_RESULT(float conc_float_value) {
		RealExpression symb_int_value = env.heap.getField(
				Types.JAVA_LANG_FLOAT, "$floatValue", conc_float, symb_float,
				conc_float_value);

		replaceTopFp32(symb_int_value);
	}

}
