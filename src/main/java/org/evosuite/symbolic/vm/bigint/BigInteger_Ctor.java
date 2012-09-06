package org.evosuite.symbolic.vm.bigint;

import java.math.BigInteger;

import org.evosuite.symbolic.expr.bv.StringToIntegerCast;
import org.evosuite.symbolic.expr.str.StringValue;
import org.evosuite.symbolic.vm.Function;
import org.evosuite.symbolic.vm.NonNullReference;
import org.evosuite.symbolic.vm.Reference;
import org.evosuite.symbolic.vm.SymbolicEnvironment;
import org.evosuite.symbolic.vm.SymbolicHeap;

public final class BigInteger_Ctor extends Function {

	public BigInteger_Ctor(SymbolicEnvironment env) {
		super(env, Types.JAVA_MATH_BIG_INTEGER, Types.INIT, Types.STRING_TO_VOID);
	}

	private String conc_string;
	private NonNullReference str_ref;

	@Override
	public void CALLER_STACK_PARAM(int nr, int calleeLocalsIndex, Object value) {

		if (value != null) {
			conc_string = (String) value;
			Reference ref = this.env.topFrame().operandStack.peekRef();
			str_ref = (NonNullReference) ref;
		} else {
			conc_string = null;
			str_ref = null;
		}

	}

	@Override
	public void CALL_RESULT() {

		if (conc_string != null && str_ref != null) {

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
		}

	}
}
