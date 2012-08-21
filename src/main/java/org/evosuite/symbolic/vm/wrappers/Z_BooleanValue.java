package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class Z_BooleanValue extends Function {

	private NonNullReference symb_boolean;
	private Boolean conc_boolean;

	private static final String BOOLEAN_VALUE = "booleanValue";

	public Z_BooleanValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_BYTE, BOOLEAN_VALUE, Types.TO_BYTE);
	}

	@Override
	public void INVOKEVIRTUAL(Object conc_boolean) {
		if (conc_boolean == null)
			return;

		symb_boolean = (NonNullReference) this.env.topFrame().operandStack
				.peekRef();
		this.conc_boolean = (Boolean) conc_boolean;
	}

	@Override
	public void CALL_RESULT(boolean conc_boolean_value) {
		IntegerExpression symb_boolean_value = env.heap.getField(
				Types.JAVA_LANG_BOOLEAN, SymbolicHeap.$BOOLEAN_VALUE,
				conc_boolean, symb_boolean, conc_boolean_value ? 1 : 0);

		replaceTopBv32(symb_boolean_value);
	}

}
