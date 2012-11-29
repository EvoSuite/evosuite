package org.evosuite.symbolic.vm.wrappers;

import org.evosuite.symbolic.expr.bv.IntegerValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class C_ValueOf extends SymbolicFunction {

	private static final String VALUE_OF = "valueOf";

	public C_ValueOf(SymbolicEnvironment env) {
		super(env, Types.JAVA_LANG_CHARACTER, VALUE_OF, Types.C_TO_CHARACTER);
	}

	@Override
	public Object executeFunction() {

		IntegerValue int_value = this.getSymbIntegerArgument(0);
		NonNullReference symb_character = (NonNullReference) this
				.getSymbRetVal();
		Character conc_character = (Character) this.getConcRetVal();

		env.heap.putField(Types.JAVA_LANG_CHARACTER, SymbolicHeap.$CHAR_VALUE,
				conc_character, symb_character, int_value);

		return symb_character;
	}

}
