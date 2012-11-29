package org.evosuite.symbolic.vm.bigint;

import java.math.BigInteger;

import org.evosuite.symbolic.expr.bv.StringToIntegerCast;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.SymbolicFunction;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class BigInteger_Ctor extends SymbolicFunction {

	public BigInteger_Ctor(SymbolicEnvironment env) {
		super(env, Types.JAVA_MATH_BIG_INTEGER, Types.INIT, Types.STRING_TO_VOID);
	}



	@Override
	public Object executeFunction() {
		String conc_string = (String) this.getConcArgument(0);
		NonNullReference str_ref = (NonNullReference) this.getSymbArgument(0);

		StringValue symb_string = this.env.heap.getField(
				Types.JAVA_LANG_STRING, SymbolicHeap.$STRING_VALUE,
				conc_string, str_ref, conc_string);

		if (symb_string.containsSymbolicVariable()) {

			NonNullReference symb_big_integer = (NonNullReference) env
					.topFrame().operandStack.peekRef();

			BigInteger bigInteger = new BigInteger(conc_string);
			long concVal = bigInteger.longValue();

			StringToIntegerCast big_integer_value = new StringToIntegerCast(
					symb_string, concVal);

			env.heap.putField(Types.JAVA_MATH_BIG_INTEGER,
					SymbolicHeap.$BIG_INTEGER_CONTENTS,
					null /* conc_big_integer */, symb_big_integer,
					big_integer_value);
		}

		// return void
		return null;
	}
}
