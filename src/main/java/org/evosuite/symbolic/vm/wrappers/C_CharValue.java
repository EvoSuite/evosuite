package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class C_CharValue extends SymbolicFunction {

	private static final String CHAR_VALUE = "charValue";

	public C_CharValue(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_CHARACTER, CHAR_VALUE, Types.TO_CHAR);
	}

	@Override
	public Object executeFunction() {

		NonNullReference symb_character = this.getSymbReceiver();
		Character conc_character = (Character) this.getConcReceiver();
		char conc_char_value = this.getConcCharRetVal();

		IntegerValue symb_char_value = env.heap.getField(
				Types.JAVA_LANG_CHARACTER, SymbolicHeap.$CHAR_VALUE,
				conc_character, symb_character, conc_char_value);

		return symb_char_value;
	}

}
