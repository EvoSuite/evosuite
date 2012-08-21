package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.IntegerExpression;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class C_CharValue extends Function {

	private NonNullReference symb_character;
	private Character conc_character;

	private static final String CHAR_VALUE = "charValue";

	public C_CharValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_CHARACTER, CHAR_VALUE, Types.TO_BYTE);
	}

	@Override
	public void INVOKEVIRTUAL(Object conc_character) {
		if (conc_character == null)
			return;

		symb_character = (NonNullReference) this.env.topFrame().operandStack
				.peekRef();
		this.conc_character = (Character) conc_character;
	}

	@Override
	public void CALL_RESULT(int conc_char_value) {
		IntegerExpression symb_char_value = env.heap.getField(
				Types.JAVA_LANG_CHARACTER, SymbolicHeap.$CHAR_VALUE,
				conc_character, symb_character, conc_char_value);

		replaceTopBv32(symb_char_value);
	}

}
